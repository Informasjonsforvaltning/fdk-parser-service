package no.digdir.fdk.parserservice.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics

object RdfParseEventMetrics {
    private var registry: MeterRegistry = Metrics.globalRegistry

    fun bind(registry: MeterRegistry) {
        this.registry = registry
    }

    fun recordPublish(
        type: String,
        kind: PublishKind,
        outcome: PublishOutcome,
    ) {
        registry
            .counter(
                "rdf_parse_event_publish_total",
                "status",
                outcome.status,
                "reason",
                outcome.reason,
                "type",
                type,
                "kind",
                kind.label,
            ).increment()
    }

    enum class PublishKind(
        val label: String,
    ) {
        PARSED("parsed"),
        HARVEST("harvest"),
    }

    enum class PublishOutcome(
        val status: String,
        val reason: String,
    ) {
        SUCCESS("success", "published"),
        PUBLISH_FAILED("error", "publish_failed"),
        SKIPPED("error", "skipped"),
    }
}
