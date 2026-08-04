package no.digdir.fdk.parserservice.metrics

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import no.fdk.rdf.parse.RdfParseResourceType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object KafkaParseMetrics {
    private var registry: MeterRegistry = Metrics.globalRegistry
    private val listenerPausedState = ConcurrentHashMap<String, AtomicInteger>()
    private val registeredGauges = ConcurrentHashMap.newKeySet<String>()

    fun bind(registry: MeterRegistry) {
        if (this.registry !== registry) {
            this.registry = registry
            registeredGauges.clear()
        }
    }

    fun registerListenerPausedGauge(listenerId: String) {
        ensureListenerPausedGaugeRegistered(listenerId)
    }

    fun setListenerPaused(
        listenerId: String,
        paused: Boolean,
    ) {
        ensureListenerPausedGaugeRegistered(listenerId).set(if (paused) 1 else 0)
    }

    fun recordEventProcessed(
        resourceType: RdfParseResourceType?,
        result: EventProcessingResult,
    ) {
        registry
            .counter(
                "rdf_parse_event_processing_total",
                "type",
                resourceType?.let { ParseMetrics.metricType(it) } ?: "unknown",
                "result",
                result.label,
            ).increment()
    }

    private fun ensureListenerPausedGaugeRegistered(listenerId: String): AtomicInteger {
        val state = listenerPausedState.computeIfAbsent(listenerId) { AtomicInteger(0) }
        if (registeredGauges.add(listenerId)) {
            Gauge
                .builder("kafka_listener_paused") { state.get().toDouble() }
                .description("1 when the given rdf-parse Kafka listener is paused, otherwise 0")
                .tag("listener", listenerId)
                .register(registry)
        }
        return state
    }

    enum class EventProcessingResult(
        val label: String,
    ) {
        ACKED("acked"),
        NACKED("nacked"),
        SKIPPED("skipped"),
        CIRCUIT_OPEN("circuit_open"),
    }
}
