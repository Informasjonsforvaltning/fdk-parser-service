package no.digdir.fdk.parserservice.configuration

import no.fdk.rdf.parse.RdfParseEvent
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

open class KafkaProducerConfig {
    @Bean
    open fun kafkaTemplate(producerFactory: ProducerFactory<String, RdfParseEvent>): KafkaTemplate<String, RdfParseEvent> {
        return KafkaTemplate(producerFactory)
    }
}
