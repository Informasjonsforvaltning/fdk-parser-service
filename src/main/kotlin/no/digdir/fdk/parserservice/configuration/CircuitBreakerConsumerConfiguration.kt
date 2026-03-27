package no.digdir.fdk.parserservice.configuration

import io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent
import no.digdir.fdk.parserservice.kafka.KafkaManager
import no.digdir.fdk.parserservice.model.RecoverableParseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class CircuitBreakerConsumerConfiguration(
    private val kafkaManager: KafkaManager,
) {
    private val circuitBreakerToListenerMapping =
        mapOf(
            "rdf-parse-data-service" to "data-service-event-consumer",
            "rdf-parse-dataset" to "dataset-event-consumer",
            "rdf-parse-event" to "event-event-consumer",
            "rdf-parse-information-model" to "information-model-event-consumer",
            "rdf-parse-service" to "service-event-consumer",
            "rdf-parse-generic" to null,
        )

    @Bean
    fun circuitBreakerRegistry(): CircuitBreakerRegistry {
        val defaultConfig =
            CircuitBreakerConfig
                .custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50f)
                .permittedNumberOfCallsInHalfOpenState(3)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .ignoreExceptions(RecoverableParseException::class.java)
                .build()

        val registry = CircuitBreakerRegistry.of(defaultConfig)
        LOGGER.debug("Configuring circuit breaker event listeners")
        circuitBreakerToListenerMapping.forEach { (circuitBreakerName, listenerId) ->
            configureCircuitBreaker(registry, circuitBreakerName, listenerId)
        }
        return registry
    }

    private fun configureCircuitBreaker(
        registry: CircuitBreakerRegistry,
        circuitBreakerName: String,
        listenerId: String?,
    ) {
        registry
            .circuitBreaker(circuitBreakerName)
            .eventPublisher
            .onStateTransition { event: CircuitBreakerOnStateTransitionEvent ->
                LOGGER.info("Circuit breaker '$circuitBreakerName' state transition: ${event.stateTransition}")
                if (listenerId != null) {
                    when (event.stateTransition) {
                        StateTransition.CLOSED_TO_OPEN,
                        StateTransition.CLOSED_TO_FORCED_OPEN,
                        StateTransition.HALF_OPEN_TO_OPEN,
                        -> kafkaManager.pause(listenerId)

                        StateTransition.OPEN_TO_HALF_OPEN,
                        StateTransition.HALF_OPEN_TO_CLOSED,
                        StateTransition.FORCED_OPEN_TO_CLOSED,
                        StateTransition.FORCED_OPEN_TO_HALF_OPEN,
                        -> kafkaManager.resume(listenerId)

                        else -> throw IllegalStateException("Unknown transition state: " + event.stateTransition)
                    }
                }
            }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CircuitBreakerConsumerConfiguration::class.java)
    }
}
