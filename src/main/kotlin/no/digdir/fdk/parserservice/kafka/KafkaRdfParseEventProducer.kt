package no.digdir.fdk.parserservice.kafka

import no.fdk.rdf.parse.RdfParseEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component


@Component
class KafkaRdfParseEventProducer(
        private val kafkaTemplate: KafkaTemplate<String, RdfParseEvent>
) {
    fun sendMessage(msg: RdfParseEvent) {
        LOGGER.debug("Sending message to Kafka topic: $TOPIC_NAME")
        if (msg.data.isEmpty()) {
            LOGGER.error("Message data is empty, not sending to Kafka - id: ${msg.fdkId}")
            return
        }
        kafkaTemplate.send(TOPIC_NAME, msg)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaRdfParseEventProducer::class.java)
        private const val TOPIC_NAME = "rdf-parse-events"
    }
}
