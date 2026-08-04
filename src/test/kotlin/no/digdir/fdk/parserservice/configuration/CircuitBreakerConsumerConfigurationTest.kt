package no.digdir.fdk.parserservice.configuration

import io.mockk.mockk
import io.mockk.verify
import no.digdir.fdk.parserservice.kafka.KafkaManager
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class CircuitBreakerConsumerConfigurationTest {
    private val circuitBreakerToListenerMapping =
        mapOf(
            "rdf-parse-concept" to "concept-event-consumer",
            "rdf-parse-data-service" to "data-service-event-consumer",
            "rdf-parse-dataset" to "dataset-event-consumer",
            "rdf-parse-event" to "event-event-consumer",
            "rdf-parse-information-model" to "information-model-event-consumer",
            "rdf-parse-service" to "service-event-consumer",
        )

    @Test
    fun `opening a circuit breaker should pause its kafka listener`() {
        val kafkaManager = mockk<KafkaManager>(relaxed = true)
        val configuration = CircuitBreakerConsumerConfiguration(kafkaManager)
        val registry = configuration.circuitBreakerRegistry()

        circuitBreakerToListenerMapping.forEach { (circuitBreakerName, listenerId) ->
            registry.circuitBreaker(circuitBreakerName).transitionToOpenState()
            verify { kafkaManager.pause(listenerId) }
        }
    }

    @Test
    fun `concept circuit breaker open should pause concept event consumer`() {
        val kafkaManager = mockk<KafkaManager>(relaxed = true)
        val configuration = CircuitBreakerConsumerConfiguration(kafkaManager)
        val registry = configuration.circuitBreakerRegistry()

        registry.circuitBreaker("rdf-parse-concept").transitionToOpenState()

        verify { kafkaManager.pause("concept-event-consumer") }
    }
}
