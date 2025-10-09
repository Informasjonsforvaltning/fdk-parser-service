package no.digdir.fdk.parseservice.kafka

import no.digdir.fdk.parseservice.model.RecoverableParseException
import no.digdir.fdk.parseservice.model.UnrecoverableParseException
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class KafkaReasonedEventConsumer(
    private val circuitBreaker: KafkaReasonedEventCircuitBreaker
) {
    @KafkaListener(
        topics = ["dataset-events"],
        groupId = "fdk-parser-service",
        concurrency = "4",
        containerFactory = "kafkaListenerContainerFactory",
        id = "rdf-parse"
    )
    fun listen(record: ConsumerRecord<String, SpecificRecord>, ack: Acknowledgment) {
        try {
            circuitBreaker.process(record)
            ack.acknowledge()
        } catch (e: RecoverableParseException) {
            ack.acknowledge()
        } catch (e: UnrecoverableParseException) {
            ack.nack(Duration.ZERO)
        }
    }
}
