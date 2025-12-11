package no.digdir.fdk.parserservice.kafka

import no.fdk.harvest.HarvestEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaHarvestEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, HarvestEvent>,
) {
    fun sendMessage(msg: HarvestEvent) {
        LOGGER.debug("Sending harvest event to Kafka topic: $TOPIC_NAME")
        kafkaTemplate.send(TOPIC_NAME, msg)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaHarvestEventProducer::class.java)
        private const val TOPIC_NAME = "harvest-events"
    }
}
