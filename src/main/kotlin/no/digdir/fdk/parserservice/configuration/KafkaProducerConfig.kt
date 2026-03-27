package no.digdir.fdk.parserservice.configuration

import io.confluent.kafka.serializers.KafkaAvroSerializer
import no.fdk.harvest.HarvestEvent
import no.fdk.rdf.parse.RdfParseEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
open class KafkaProducerConfig(
    @param:Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @param:Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String,
) {
    @Bean
    open fun rdfParseEventProducerFactory(): ProducerFactory<String, RdfParseEvent> = DefaultKafkaProducerFactory(producerProperties())

    @Bean
    open fun harvestEventProducerFactory(): ProducerFactory<String, HarvestEvent> = DefaultKafkaProducerFactory(producerProperties())

    @Bean
    open fun kafkaTemplate(rdfParseEventProducerFactory: ProducerFactory<String, RdfParseEvent>): KafkaTemplate<String, RdfParseEvent> =
        KafkaTemplate(rdfParseEventProducerFactory)

    @Bean
    open fun harvestEventKafkaTemplate(
        harvestEventProducerFactory: ProducerFactory<String, HarvestEvent>,
    ): KafkaTemplate<String, HarvestEvent> = KafkaTemplate(harvestEventProducerFactory)

    private fun producerProperties(): MutableMap<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        props[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "snappy"
        props["schema.registry.url"] = schemaRegistryUrl
        props["value.subject.name.strategy"] = "io.confluent.kafka.serializers.subject.RecordNameStrategy"
        return props
    }
}
