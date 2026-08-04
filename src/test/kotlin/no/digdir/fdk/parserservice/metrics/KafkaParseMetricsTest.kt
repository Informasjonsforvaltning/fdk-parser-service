package no.digdir.fdk.parserservice.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.fdk.rdf.parse.RdfParseResourceType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class KafkaParseMetricsTest {
    private lateinit var registry: SimpleMeterRegistry

    @BeforeEach
    fun setUp() {
        registry = SimpleMeterRegistry()
        KafkaParseMetrics.bind(registry)
    }

    @AfterEach
    fun tearDown() {
        KafkaParseMetrics.setListenerPaused("concept-event-consumer", false)
        KafkaParseMetrics.setListenerPaused("dataset-event-consumer", false)
        registry.clear()
    }

    @Test
    fun `recordEventProcessed increments rdf_parse_event_processing_total`() {
        KafkaParseMetrics.recordEventProcessed(
            RdfParseResourceType.DATASET,
            KafkaParseMetrics.EventProcessingResult.ACKED,
        )
        KafkaParseMetrics.recordEventProcessed(
            null,
            KafkaParseMetrics.EventProcessingResult.SKIPPED,
        )
        KafkaParseMetrics.recordEventProcessed(
            RdfParseResourceType.CONCEPT,
            KafkaParseMetrics.EventProcessingResult.NACKED,
        )
        KafkaParseMetrics.recordEventProcessed(
            null,
            KafkaParseMetrics.EventProcessingResult.CIRCUIT_OPEN,
        )

        assertEquals(
            1.0,
            registry
                .counter(
                    "rdf_parse_event_processing_total",
                    "type",
                    "dataset",
                    "result",
                    "acked",
                ).count(),
        )
        assertEquals(
            1.0,
            registry
                .counter(
                    "rdf_parse_event_processing_total",
                    "type",
                    "unknown",
                    "result",
                    "skipped",
                ).count(),
        )
        assertEquals(
            1.0,
            registry
                .counter(
                    "rdf_parse_event_processing_total",
                    "type",
                    "concept",
                    "result",
                    "nacked",
                ).count(),
        )
        assertEquals(
            1.0,
            registry
                .counter(
                    "rdf_parse_event_processing_total",
                    "type",
                    "unknown",
                    "result",
                    "circuit_open",
                ).count(),
        )
    }

    @Test
    fun `registerListenerPausedGauge exposes gauge before first state change`() {
        KafkaParseMetrics.registerListenerPausedGauge("concept-event-consumer")

        assertEquals(
            0.0,
            registry
                .find("kafka_listener_paused")
                .tag("listener", "concept-event-consumer")
                .gauge()
                ?.value(),
        )
    }

    @Test
    fun `setListenerPaused updates kafka_listener_paused gauge per listener`() {
        KafkaParseMetrics.setListenerPaused("concept-event-consumer", true)
        assertEquals(
            1.0,
            registry
                .find("kafka_listener_paused")
                .tag("listener", "concept-event-consumer")
                .gauge()
                ?.value(),
        )
        assertEquals(
            null,
            registry
                .find("kafka_listener_paused")
                .tag("listener", "dataset-event-consumer")
                .gauge(),
        )

        KafkaParseMetrics.setListenerPaused("concept-event-consumer", false)
        assertEquals(
            0.0,
            registry
                .find("kafka_listener_paused")
                .tag("listener", "concept-event-consumer")
                .gauge()
                ?.value(),
        )
    }
}
