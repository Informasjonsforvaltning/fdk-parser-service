package no.digdir.fdk.parserservice.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class RdfParseEventMetricsTest {
    private lateinit var registry: SimpleMeterRegistry

    @BeforeEach
    fun setUp() {
        registry = SimpleMeterRegistry()
        RdfParseEventMetrics.bind(registry)
    }

    @AfterEach
    fun tearDown() {
        registry.clear()
    }

    @Test
    fun `recordPublish increments rdf_parse_event_publish_total`() {
        RdfParseEventMetrics.recordPublish(
            type = "dataset",
            kind = RdfParseEventMetrics.PublishKind.PARSED,
            outcome = RdfParseEventMetrics.PublishOutcome.SUCCESS,
        )
        RdfParseEventMetrics.recordPublish(
            type = "concept",
            kind = RdfParseEventMetrics.PublishKind.HARVEST,
            outcome = RdfParseEventMetrics.PublishOutcome.PUBLISH_FAILED,
        )
        RdfParseEventMetrics.recordPublish(
            type = "event",
            kind = RdfParseEventMetrics.PublishKind.PARSED,
            outcome = RdfParseEventMetrics.PublishOutcome.SKIPPED,
        )

        assertEquals(
            1.0,
            registry
                .counter(
                    "rdf_parse_event_publish_total",
                    "status",
                    "success",
                    "reason",
                    "published",
                    "type",
                    "dataset",
                    "kind",
                    "parsed",
                ).count(),
        )
        assertEquals(
            1.0,
            registry
                .counter(
                    "rdf_parse_event_publish_total",
                    "status",
                    "error",
                    "reason",
                    "publish_failed",
                    "type",
                    "concept",
                    "kind",
                    "harvest",
                ).count(),
        )
        assertEquals(
            1.0,
            registry
                .counter(
                    "rdf_parse_event_publish_total",
                    "status",
                    "error",
                    "reason",
                    "skipped",
                    "type",
                    "event",
                    "kind",
                    "parsed",
                ).count(),
        )
    }
}
