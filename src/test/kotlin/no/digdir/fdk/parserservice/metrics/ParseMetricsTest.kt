package no.digdir.fdk.parserservice.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.digdir.fdk.parserservice.model.NoAcceptableFDKRecordsException
import no.digdir.fdk.parserservice.model.RecoverableParseException
import no.digdir.fdk.parserservice.model.UnrecoverableParseException
import no.fdk.rdf.parse.RdfParseResourceType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

@Tag("unit")
class ParseMetricsTest {
    private lateinit var registry: SimpleMeterRegistry

    @BeforeEach
    fun setUp() {
        registry = SimpleMeterRegistry()
        ParseMetrics.bind(registry)
    }

    @AfterEach
    fun tearDown() {
        registry.clear()
    }

    @Test
    fun `recordTotal records rdf_parse timer`() {
        ParseMetrics.recordTotal(RdfParseResourceType.DATASET, 100.milliseconds)

        assertEquals(
            1L,
            registry
                .timer("rdf_parse", "type", "dataset")
                .count(),
        )
    }

    @Test
    fun `recordError increments rdf_parse_error with a reason tag derived from the exception class name`() {
        ParseMetrics.recordError(RdfParseResourceType.CONCEPT, NoAcceptableFDKRecordsException("no records"))

        assertEquals(
            1.0,
            registry
                .counter("rdf_parse_error", "type", "concept", "reason", "no_acceptable_fdk_records")
                .count(),
        )
    }

    @Test
    fun `recordError falls back to the base class name when a generic exception is used`() {
        ParseMetrics.recordError(RdfParseResourceType.CONCEPT, UnrecoverableParseException("boom"))
        ParseMetrics.recordError(RdfParseResourceType.CONCEPT, RecoverableParseException("boom"))

        assertEquals(
            1.0,
            registry
                .counter("rdf_parse_error", "type", "concept", "reason", "unrecoverable_parse")
                .count(),
        )
        assertEquals(
            1.0,
            registry
                .counter("rdf_parse_error", "type", "concept", "reason", "recoverable_parse")
                .count(),
        )
    }

    @Test
    fun `recordProfileMatch increments rdf_parse_profile_match_total with the correct status`() {
        ParseMetrics.recordProfileMatch(RdfParseResourceType.DATASET, "DCAT-AP-NO-V3", matched = true)
        ParseMetrics.recordProfileMatch(RdfParseResourceType.DATASET, "DCAT-AP-NO-V1", matched = false)

        assertEquals(
            1.0,
            registry
                .counter("rdf_parse_profile_match_total", "type", "dataset", "parser", "DCAT-AP-NO-V3", "status", "matched")
                .count(),
        )
        assertEquals(
            1.0,
            registry
                .counter("rdf_parse_profile_match_total", "type", "dataset", "parser", "DCAT-AP-NO-V1", "status", "failed")
                .count(),
        )
    }

    @Test
    fun `recordPipelineLag records rdf_parse_pipeline_lag timer`() {
        ParseMetrics.recordPipelineLag(RdfParseResourceType.EVENT, 250)

        assertEquals(
            1L,
            registry
                .timer("rdf_parse_pipeline_lag", "type", "event")
                .count(),
        )
    }

    @Test
    fun `recordPipelineLag clamps negative lag to zero`() {
        ParseMetrics.recordPipelineLag(RdfParseResourceType.EVENT, -500)

        val timer = registry.timer("rdf_parse_pipeline_lag", "type", "event")
        assertEquals(1L, timer.count())
        assertEquals(0.0, timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS))
    }

    @Test
    fun `recordPayloadSize records rdf_parse_payload_size_chars summary tagged by direction`() {
        ParseMetrics.recordPayloadSize(RdfParseResourceType.SERVICE, ParseMetrics.PayloadDirection.INPUT, 1000)
        ParseMetrics.recordPayloadSize(RdfParseResourceType.SERVICE, ParseMetrics.PayloadDirection.OUTPUT, 500)

        assertEquals(
            1000.0,
            registry
                .summary("rdf_parse_payload_size_chars", "type", "service", "direction", "input")
                .totalAmount(),
        )
        assertEquals(
            500.0,
            registry
                .summary("rdf_parse_payload_size_chars", "type", "service", "direction", "output")
                .totalAmount(),
        )
    }

    @Test
    fun `metricType lowercases the resource type name`() {
        assertEquals("data_service", ParseMetrics.metricType(RdfParseResourceType.DATA_SERVICE))
        assertEquals("information_model", ParseMetrics.metricType(RdfParseResourceType.INFORMATION_MODEL))
        assertEquals("service", ParseMetrics.metricType(RdfParseResourceType.SERVICE))
    }
}
