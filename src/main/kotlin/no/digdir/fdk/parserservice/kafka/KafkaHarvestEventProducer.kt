package no.digdir.fdk.parserservice.kafka

import no.digdir.fdk.parserservice.metrics.RdfParseEventMetrics
import no.digdir.fdk.parserservice.metrics.RdfParseEventMetrics.PublishKind
import no.digdir.fdk.parserservice.metrics.RdfParseEventMetrics.PublishOutcome
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
        val type = msg.dataType.name.lowercase()
        try {
            kafkaTemplate
                .send(TOPIC_NAME, msg)
                .whenComplete { _, ex ->
                    RdfParseEventMetrics.recordPublish(
                        type,
                        PublishKind.HARVEST,
                        if (ex == null) PublishOutcome.SUCCESS else PublishOutcome.PUBLISH_FAILED,
                    )
                    if (ex != null) {
                        LOGGER.error("Failed to produce harvest event for fdkId={} dataType={}", msg.fdkId, msg.dataType, ex)
                    }
                }
        } catch (e: Exception) {
            RdfParseEventMetrics.recordPublish(type, PublishKind.HARVEST, PublishOutcome.PUBLISH_FAILED)
            LOGGER.error("Failed to enqueue harvest event for fdkId={} dataType={}", msg.fdkId, msg.dataType, e)
            throw e
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaHarvestEventProducer::class.java)
        private const val TOPIC_NAME = "harvest-events"
    }
}
