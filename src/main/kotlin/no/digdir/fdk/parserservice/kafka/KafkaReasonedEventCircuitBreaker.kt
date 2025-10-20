package no.digdir.fdk.parserservice.kafka

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.micrometer.core.instrument.Metrics
import no.digdir.fdk.parserservice.handler.DatasetHandler
import no.digdir.fdk.parserservice.model.RecoverableParseException
import no.digdir.fdk.parserservice.model.UnrecoverableParseException
import no.fdk.dataset.DatasetEvent
import no.fdk.dataset.DatasetEventType
import no.fdk.rdf.parse.RdfParseEvent
import no.fdk.rdf.parse.RdfParseResourceType
import org.apache.avro.generic.GenericRecord
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
    @CircuitBreaker(name = "rdf-parse-generic")
    open fun processGeneric(event: GenericRecord) {
        val type = runCatching { event.get("type") as String }.getOrNull()
        val resourceType = when (type) {
            DatasetEventType.DATASET_REASONED.name -> RdfParseResourceType.DATASET
            else -> null
        }

        val fdkId = runCatching { event.get("fdkId") as String }.getOrNull()
        val graph = runCatching { event.get("graph") as String }.getOrNull()
        val timestamp = runCatching { event.get("timestamp") as Long }.getOrNull()

        if (fdkId != null && graph != null && timestamp != null && resourceType != null) {
            handleRecord(fdkId, graph, timestamp, resourceType)
        } else {
            LOGGER.error("Unable to get required message values. fdkId: {}, graph: {}, timestamp: {}, type: {}", fdkId, graph, timestamp, resourceType)
            throw UnrecoverableParseException("Unable to get required message values")
        }
    }

    @CircuitBreaker(name = "rdf-parse-dataset")
    open fun processDataset(event: DatasetEvent) {
        val type = runCatching { event.type }.getOrNull()
        if (type == DatasetEventType.DATASET_REASONED) {
            val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
            val graph = runCatching { event.graph.toString() }.getOrNull()
            val timestamp = runCatching { event.timestamp }.getOrNull()

            if (fdkId != null && graph != null && timestamp != null) {
                handleRecord(fdkId, graph, timestamp, RdfParseResourceType.DATASET)
            } else {
                LOGGER.error("Unable to get required dataset message values. fdkId: {}, graph: {}, timestamp: {}", fdkId, graph, timestamp)
                throw UnrecoverableParseException("Unable to get required dataset message values")
            }
        }
    }

    private fun handleRecord(fdkId: String, graph: String, timestamp: Long, resourceType: RdfParseResourceType) {
        try {
            parseAndProduce(fdkId, graph, timestamp, resourceType)
        } catch (e: RecoverableParseException) {
            LOGGER.warn("Recoverable parsing error: " + e.message)
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
                RdfParseResourceType.CONCEPT -> LOGGER.warn("Parse of concepts not implemented")
                RdfParseResourceType.DATA_SERVICE -> LOGGER.warn("Parse of data services not implemented")
                RdfParseResourceType.DATASET -> datasetHandler.parseDataset(fdkId, graph)
                RdfParseResourceType.EVENT -> LOGGER.warn("Parse of events not implemented")
                RdfParseResourceType.INFORMATION_MODEL -> LOGGER.warn("Parse of information models not implemented")
                RdfParseResourceType.SERVICE -> LOGGER.warn("Parse of services not implemented")
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
