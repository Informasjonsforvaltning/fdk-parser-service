package no.digdir.fdk.parserservice.configuration

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.kafka.listener.ContainerProperties
import java.time.Duration

@Tag("unit")
class KafkaConsumerConfigTest {
    private val config =
        KafkaConsumerConfig(
            bootstrapServers = "localhost:9092",
            securityProtocol = "SASL_PLAINTEXT",
            saslMechanism = "SCRAM-SHA-512",
            saslJaasConfig = "jaas-config",
            schemaRegistryUrl = "http://localhost:8081",
            meterRegistry = SimpleMeterRegistry(),
        )

    @Test
    fun consumerFactoryCarriesSecuritySettings() {
        val props = config.consumerFactory().configurationProperties

        assertEquals("SASL_PLAINTEXT", props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG])
        assertEquals("SCRAM-SHA-512", props[SaslConfigs.SASL_MECHANISM])
        assertEquals("jaas-config", props[SaslConfigs.SASL_JAAS_CONFIG])
    }

    @Test
    fun listenerContainerFactoryRetriesOnAuthFailure() {
        val containerProperties = config.kafkaListenerContainerFactory().containerProperties

        assertEquals(ContainerProperties.AckMode.MANUAL_IMMEDIATE, containerProperties.ackMode)
        assertEquals(Duration.ofSeconds(30), containerProperties.authExceptionRetryInterval)
    }
}
