package no.digdir.fdk.parserservice.kafka

import no.digdir.fdk.parserservice.metrics.ParseMetrics
import no.digdir.fdk.parserservice.metrics.RdfParseEventMetrics
import no.digdir.fdk.parserservice.metrics.RdfParseEventMetrics.PublishKind
import no.digdir.fdk.parserservice.metrics.RdfParseEventMetrics.PublishOutcome
import no.fdk.rdf.parse.RdfParseEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaRdfParseEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, RdfParseEvent>,
) {
    fun sendMessage(msg: RdfParseEvent) {
        LOGGER.debug("Sending message to Kafka topic: $TOPIC_NAME")
        val type = ParseMetrics.metricType(msg.resourceType)
        if (msg.data.isEmpty()) {
            LOGGER.error("Message data is empty, not sending to Kafka - id: ${msg.fdkId}")
            RdfParseEventMetrics.recordPublish(type, PublishKind.PARSED, PublishOutcome.SKIPPED)
            return
        }
        try {
            kafkaTemplate
                .send(TOPIC_NAME, msg)
                .whenComplete { _, ex ->
                    RdfParseEventMetrics.recordPublish(
                        type,
                        PublishKind.PARSED,
                        if (ex == null) PublishOutcome.SUCCESS else PublishOutcome.PUBLISH_FAILED,
                    )
                    if (ex != null) {
                        LOGGER.error("Failed to produce rdf parse event for fdkId={} resourceType={}", msg.fdkId, msg.resourceType, ex)
                    }
                }
        } catch (e: Exception) {
            RdfParseEventMetrics.recordPublish(type, PublishKind.PARSED, PublishOutcome.PUBLISH_FAILED)
            LOGGER.error("Failed to enqueue rdf parse event for fdkId={} resourceType={}", msg.fdkId, msg.resourceType, e)
            throw e
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaRdfParseEventProducer::class.java)
        private const val TOPIC_NAME = "rdf-parse-events"
    }
}
