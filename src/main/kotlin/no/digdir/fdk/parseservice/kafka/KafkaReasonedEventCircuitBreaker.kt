package no.digdir.fdk.parseservice.kafka

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.micrometer.core.instrument.Metrics
import no.digdir.fdk.parseservice.exception.RecoverableParseException
import no.digdir.fdk.parseservice.exception.UnrecoverableParseException
import no.digdir.fdk.parseservice.handler.DatasetHandler
import no.fdk.dataset.DatasetEvent
import no.fdk.dataset.DatasetEventType
import no.fdk.rdf.parse.RdfParseEvent
import no.fdk.rdf.parse.RdfParseResourceType
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.time.measureTimedValue
import kotlin.time.toJavaDuration


@Component
open class KafkaReasonedEventCircuitBreaker(
    private val producer: KafkaRdfParseEventProducer,
    private val datasetHandler: DatasetHandler
) {
    @CircuitBreaker(name = "rdf-parse")
    open fun process(record: ConsumerRecord<String, SpecificRecord>) {
        LOGGER.debug("Received message - offset: " + record.offset())
        val event = record.value()

        val resourceType = when (event) {
            is DatasetEvent -> RdfParseResourceType.DATASET
            else -> throw UnrecoverableParseException("Unknown event type")
        }

        try {
            event.let {
                if (it.type == DatasetEventType.DATASET_REASONED) {
                    parseAndProduce(it.fdkId.toString(), it.graph.toString(), it.timestamp, resourceType)
                }
            }
        } catch (e: RecoverableParseException) {
            LOGGER.debug("Recoverable parsing error: " + e.message)
            Metrics.counter(
                "rdf_parse_error",
                "type", resourceType.toString().lowercase()
            ).increment()
            throw e
        } catch (e: UnrecoverableParseException) {
            LOGGER.error("Unrecoverable parsing error: " + e.message)
            Metrics.counter(
                "rdf_parse_error",
                "type", resourceType.toString().lowercase()
            ).increment()
            throw e
        }
    }

    private fun parseAndProduce(fdkId: String, graph: String, timestamp: Long, type: RdfParseResourceType) {
        val timeElapsed = measureTimedValue {
            LOGGER.debug("Parse dataset - id: $fdkId")
            val json = when (type) {
                RdfParseResourceType.CONCEPT -> LOGGER.error("Parse of concepts not implemented")
                RdfParseResourceType.DATA_SERVICE -> LOGGER.error("Parse of data services not implemented")
                RdfParseResourceType.DATASET -> datasetHandler.parseDataset(fdkId, graph)
                RdfParseResourceType.EVENT -> LOGGER.error("Parse of events not implemented")
                RdfParseResourceType.INFORMATION_MODEL -> LOGGER.error("Parse of information models not implemented")
                RdfParseResourceType.SERVICE -> LOGGER.error("Parse of services not implemented")
            }
            producer.sendMessage(RdfParseEvent(type, fdkId, json.toString(), timestamp))
        }
        Metrics.timer(
            "rdf_parse",
            "type", type.toString().lowercase()
        ).record(timeElapsed.duration.toJavaDuration())
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaReasonedEventCircuitBreaker::class.java)
    }
}
