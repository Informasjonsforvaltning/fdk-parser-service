package no.digdir.fdk.parserservice.kafka

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.micrometer.core.instrument.Metrics
import no.digdir.fdk.parserservice.handler.DataServiceHandler
import no.digdir.fdk.parserservice.handler.DatasetHandler
import no.digdir.fdk.parserservice.handler.EventHandler
import no.digdir.fdk.parserservice.handler.InformationModelHandler
import no.digdir.fdk.parserservice.handler.ServiceHandler
import no.digdir.fdk.parserservice.model.RecoverableParseException
import no.digdir.fdk.parserservice.model.UnrecoverableParseException
import no.fdk.dataservice.DataServiceEvent
import no.fdk.dataservice.DataServiceEventType
import no.fdk.dataset.DatasetEvent
import no.fdk.dataset.DatasetEventType
import no.fdk.event.EventEvent
import no.fdk.event.EventEventType
import no.fdk.informationmodel.InformationModelEvent
import no.fdk.informationmodel.InformationModelEventType
import no.fdk.rdf.parse.RdfParseEvent
import no.fdk.rdf.parse.RdfParseResourceType
import no.fdk.service.ServiceEvent
import no.fdk.service.ServiceEventType
import org.apache.avro.generic.GenericRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.time.measureTimedValue
import kotlin.time.toJavaDuration

@Component
open class KafkaReasonedEventCircuitBreaker(
    private val producer: KafkaRdfParseEventProducer,
    private val dataServiceHandler: DataServiceHandler,
    private val datasetHandler: DatasetHandler,
    private val eventHandler: EventHandler,
    private val informationModelHandler: InformationModelHandler,
    private val serviceHandler: ServiceHandler,
) {
    @CircuitBreaker(name = "rdf-parse-generic")
    open fun processGeneric(event: GenericRecord) {
        val type = runCatching { event.get("type") as String }.getOrNull()
        val resourceType =
            when (type) {
                DataServiceEventType.DATA_SERVICE_REASONED.name -> RdfParseResourceType.DATA_SERVICE
                DatasetEventType.DATASET_REASONED.name -> RdfParseResourceType.DATASET
                EventEventType.EVENT_REASONED.name -> RdfParseResourceType.EVENT
                InformationModelEventType.INFORMATION_MODEL_REASONED.name -> RdfParseResourceType.INFORMATION_MODEL
                ServiceEventType.SERVICE_REASONED.name -> RdfParseResourceType.SERVICE
                else -> null
            }

        val fdkId = runCatching { event.get("fdkId") as String }.getOrNull()
        val graph = runCatching { event.get("graph") as String }.getOrNull()
        val timestamp = runCatching { event.get("timestamp") as Long }.getOrNull()

        if (fdkId != null && graph != null && timestamp != null && resourceType != null) {
            handleRecord(fdkId, graph, timestamp, resourceType)
        } else {
            LOGGER.error(
                "Unable to get required message values. fdkId: {}, graph: {}, timestamp: {}, type: {}",
                fdkId,
                graph,
                timestamp,
                resourceType,
            )
            throw UnrecoverableParseException("Unable to get required message values")
        }
    }

    @CircuitBreaker(name = "rdf-parse-data-service")
    open fun processDataService(event: DataServiceEvent) {
        val type = runCatching { event.type }.getOrNull()
        if (type == DataServiceEventType.DATA_SERVICE_REASONED) {
            val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
            val graph = runCatching { event.graph.toString() }.getOrNull()
            val timestamp = runCatching { event.timestamp }.getOrNull()

            if (fdkId != null && graph != null && timestamp != null) {
                handleRecord(fdkId, graph, timestamp, RdfParseResourceType.DATA_SERVICE)
            } else {
                LOGGER.error(
                    "Unable to get required data service message values. fdkId: {}, graph: {}, timestamp: {}",
                    fdkId,
                    graph,
                    timestamp,
                )
                throw UnrecoverableParseException("Unable to get required data service message values")
            }
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

    @CircuitBreaker(name = "rdf-parse-information-model")
    open fun processInformationModel(event: InformationModelEvent) {
        val type = runCatching { event.type }.getOrNull()
        if (type == InformationModelEventType.INFORMATION_MODEL_REASONED) {
            val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
            val graph = runCatching { event.graph.toString() }.getOrNull()
            val timestamp = runCatching { event.timestamp }.getOrNull()

            if (fdkId != null && graph != null && timestamp != null) {
                handleRecord(fdkId, graph, timestamp, RdfParseResourceType.INFORMATION_MODEL)
            } else {
                LOGGER.error(
                    "Unable to get required information model message values. fdkId: {}, graph: {}, timestamp: {}",
                    fdkId,
                    graph,
                    timestamp,
                )
                throw UnrecoverableParseException("Unable to get required information model message values")
            }
        }
    }

    @CircuitBreaker(name = "rdf-parse-service")
    open fun processService(event: ServiceEvent) {
        val type = runCatching { event.type }.getOrNull()
        if (type == ServiceEventType.SERVICE_REASONED) {
            val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
            val graph = runCatching { event.graph.toString() }.getOrNull()
            val timestamp = runCatching { event.timestamp }.getOrNull()

            if (fdkId != null && graph != null && timestamp != null) {
                handleRecord(fdkId, graph, timestamp, RdfParseResourceType.SERVICE)
            } else {
                LOGGER.error(
                    "Unable to get required service message values. fdkId: {}, graph: {}, timestamp: {}",
                    fdkId,
                    graph,
                    timestamp,
                )
                throw UnrecoverableParseException("Unable to get required service message values")
            }
        }
    }

    @CircuitBreaker(name = "rdf-parse-event")
    open fun processEvent(event: EventEvent) {
        val type = runCatching { event.type }.getOrNull()
        if (type == EventEventType.EVENT_REASONED) {
            val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
            val graph = runCatching { event.graph.toString() }.getOrNull()
            val timestamp = runCatching { event.timestamp }.getOrNull()

            if (fdkId != null && graph != null && timestamp != null) {
                handleRecord(fdkId, graph, timestamp, RdfParseResourceType.EVENT)
            } else {
                LOGGER.error(
                    "Unable to get required event message values. fdkId: {}, graph: {}, timestamp: {}",
                    fdkId,
                    graph,
                    timestamp,
                )
                throw UnrecoverableParseException("Unable to get required event message values")
            }
        }
    }

    private fun handleRecord(
        fdkId: String,
        graph: String,
        timestamp: Long,
        resourceType: RdfParseResourceType,
    ) {
        try {
            parseAndProduce(fdkId, graph, timestamp, resourceType)
        } catch (e: RecoverableParseException) {
            LOGGER.warn("Recoverable parsing error: " + e.message)
            Metrics
                .counter(
                    "rdf_parse_error",
                    "type",
                    resourceType.toString().lowercase(),
                ).increment()
            throw e
        } catch (e: UnrecoverableParseException) {
            LOGGER.error("Unrecoverable parsing error: " + e.message)
            Metrics
                .counter(
                    "rdf_parse_error",
                    "type",
                    resourceType.toString().lowercase(),
                ).increment()
            throw e
        }
    }

    private fun parseAndProduce(
        fdkId: String,
        graph: String,
        timestamp: Long,
        type: RdfParseResourceType,
    ) {
        val timeElapsed =
            measureTimedValue {
                LOGGER.debug("Parse dataset - id: $fdkId")
                val json =
                    when (type) {
                        RdfParseResourceType.CONCEPT -> LOGGER.warn("Parse of concepts not implemented")
                        RdfParseResourceType.DATA_SERVICE -> dataServiceHandler.parseDataService(fdkId, graph)
                        RdfParseResourceType.DATASET -> datasetHandler.parseDataset(fdkId, graph)
                        RdfParseResourceType.EVENT -> eventHandler.parseEvent(fdkId, graph)
                        RdfParseResourceType.INFORMATION_MODEL -> informationModelHandler.parseInformationModel(fdkId, graph)
                        RdfParseResourceType.SERVICE -> serviceHandler.parseService(fdkId, graph)
                    }
                producer.sendMessage(RdfParseEvent(type, fdkId, json.toString(), timestamp))
            }
        Metrics
            .timer(
                "rdf_parse",
                "type",
                type.toString().lowercase(),
            ).record(timeElapsed.duration.toJavaDuration())
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaReasonedEventCircuitBreaker::class.java)
    }
}
