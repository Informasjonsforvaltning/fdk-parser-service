package no.digdir.fdk.parserservice.configuration

import no.fdk.harvest.HarvestEvent
import no.fdk.rdf.parse.RdfParseEvent
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

open class KafkaProducerConfig {
    @Bean
    open fun kafkaTemplate(producerFactory: ProducerFactory<String, RdfParseEvent>): KafkaTemplate<String, RdfParseEvent> =
        KafkaTemplate(producerFactory)

    @Bean
    open fun harvestEventKafkaTemplate(producerFactory: ProducerFactory<String, HarvestEvent>): KafkaTemplate<String, HarvestEvent> =
        KafkaTemplate(producerFactory)
}
