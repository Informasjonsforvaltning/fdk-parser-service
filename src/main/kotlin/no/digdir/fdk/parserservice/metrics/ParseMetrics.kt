package no.digdir.fdk.parserservice.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import no.fdk.rdf.parse.RdfParseResourceType
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toJavaDuration

object ParseMetrics {
    private var registry: MeterRegistry = Metrics.globalRegistry

    fun bind(registry: MeterRegistry) {
        this.registry = registry
    }

    fun recordTotal(
        resourceType: RdfParseResourceType,
        duration: Duration,
    ) {
        registry
            .timer(
                "rdf_parse",
                "type",
                metricType(resourceType),
            ).record(duration.toJavaDuration())
    }

    /** Records a parsing failure, tagged with a `reason` derived from the simple class name. */
    fun recordError(
        resourceType: RdfParseResourceType,
        cause: Throwable,
    ) {
        registry
            .counter(
                "rdf_parse_error",
                "type",
                metricType(resourceType),
                "reason",
                errorReason(cause),
            ).increment()
    }

    /** Records whether a specific parser implementation matched (parsed) a resource. */
    fun recordProfileMatch(
        resourceType: RdfParseResourceType,
        parserName: String,
        matched: Boolean,
    ) {
        registry
            .counter(
                "rdf_parse_profile_match_total",
                "type",
                metricType(resourceType),
                "parser",
                parserName,
                "status",
                if (matched) "matched" else "failed",
            ).increment()
    }

    /** Records the end-to-end lag between when the reasoned event was produced and when parsing started. */
    fun recordPipelineLag(
        resourceType: RdfParseResourceType,
        lagMillis: Long,
    ) {
        registry
            .timer(
                "rdf_parse_pipeline_lag",
                "type",
                metricType(resourceType),
            ).record(lagMillis.coerceAtLeast(0), TimeUnit.MILLISECONDS)
    }

    /** Records the size (in characters) of an input RDF graph or output JSON payload. */
    fun recordPayloadSize(
        resourceType: RdfParseResourceType,
        direction: PayloadDirection,
        sizeChars: Int,
    ) {
        registry
            .summary(
                "rdf_parse_payload_size_chars",
                "type",
                metricType(resourceType),
                "direction",
                direction.label,
            ).record(sizeChars.toDouble())
    }

    fun metricType(resourceType: RdfParseResourceType): String = resourceType.name.lowercase()

    private fun errorReason(cause: Throwable): String {
        val simpleName = cause::class.simpleName ?: "unknown"
        val withoutSuffix = simpleName.removeSuffix("Exception").ifEmpty { simpleName }
        return toSnakeCase(withoutSuffix)
    }

    private fun toSnakeCase(value: String): String =
        value
            .replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), "_")
            .replace(Regex("(?<=[A-Z])(?=[A-Z][a-z])"), "_")
            .lowercase()

    enum class PayloadDirection(
        val label: String,
    ) {
        INPUT("input"),
        OUTPUT("output"),
    }
}
