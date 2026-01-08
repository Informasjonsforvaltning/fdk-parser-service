package no.digdir.fdk.parserservice.configuration

import io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent
import no.digdir.fdk.parserservice.kafka.KafkaManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

@Configuration
open class CircuitBreakerConsumerConfiguration(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
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

    init {
        LOGGER.debug("Configuring circuit breaker event listeners")
        circuitBreakerToListenerMapping.forEach { (circuitBreakerName, listenerId) ->
            configureCircuitBreaker(circuitBreakerName, listenerId)
        }
    }

    private fun configureCircuitBreaker(
        circuitBreakerName: String,
        listenerId: String?,
    ) {
        circuitBreakerRegistry
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
