package no.digdir.fdk.parserservice.configuration

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class KafkaProducerConfigTest {
    private val config =
        KafkaProducerConfig(
            bootstrapServers = "localhost:9092",
            securityProtocol = "SASL_PLAINTEXT",
            saslMechanism = "SCRAM-SHA-512",
            saslJaasConfig = "jaas-config",
            schemaRegistryUrl = "http://localhost:8081",
            meterRegistry = SimpleMeterRegistry(),
        )

    @Test
    fun producerFactoriesCarrySecuritySettings() {
        listOf(
            config.rdfParseEventProducerFactory().configurationProperties,
            config.harvestEventProducerFactory().configurationProperties,
        ).forEach { props ->
            assertEquals("SASL_PLAINTEXT", props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG])
            assertEquals("SCRAM-SHA-512", props[SaslConfigs.SASL_MECHANISM])
            assertEquals("jaas-config", props[SaslConfigs.SASL_JAAS_CONFIG])
        }
    }
}
