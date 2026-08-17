package no.digdir.fdk.parserservice.configuration

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics
import io.micrometer.core.instrument.MeterRegistry
import jakarta.annotation.PostConstruct
import no.digdir.fdk.parserservice.metrics.KafkaParseMetrics
import no.digdir.fdk.parserservice.metrics.ParseMetrics
import no.digdir.fdk.parserservice.metrics.RdfParseEventMetrics
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfiguration(private val circuitBreakerRegistry: CircuitBreakerRegistry, private val meterRegistry: MeterRegistry) {
    @PostConstruct
    fun bindMetrics() {
        ParseMetrics.bind(meterRegistry)
        KafkaParseMetrics.bind(meterRegistry)
        RdfParseEventMetrics.bind(meterRegistry)

        TaggedCircuitBreakerMetrics
            .ofCircuitBreakerRegistry(circuitBreakerRegistry)
            .bindTo(meterRegistry)
    }
}
