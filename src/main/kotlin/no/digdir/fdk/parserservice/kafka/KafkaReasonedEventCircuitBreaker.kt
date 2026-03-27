package no.digdir.fdk.parserservice.kafka

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.micrometer.core.instrument.Metrics
import no.digdir.fdk.parserservice.handler.ConceptHandler
import no.digdir.fdk.parserservice.handler.DataServiceHandler
import no.digdir.fdk.parserservice.handler.DatasetHandler
import no.digdir.fdk.parserservice.handler.EventHandler
import no.digdir.fdk.parserservice.handler.InformationModelHandler
import no.digdir.fdk.parserservice.handler.ServiceHandler
import no.digdir.fdk.parserservice.model.RecoverableParseException
import no.digdir.fdk.parserservice.model.UnrecoverableParseException
import no.fdk.concept.ConceptEvent
import no.fdk.concept.ConceptEventType
import no.fdk.dataservice.DataServiceEvent
import no.fdk.dataservice.DataServiceEventType
import no.fdk.dataset.DatasetEvent
import no.fdk.dataset.DatasetEventType
import no.fdk.event.EventEvent
import no.fdk.event.EventEventType
import no.fdk.harvest.DataType
import no.fdk.harvest.HarvestEvent
import no.fdk.harvest.HarvestPhase
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
import java.time.Instant
import kotlin.time.measureTimedValue
import kotlin.time.toJavaDuration

@Component
class KafkaReasonedEventCircuitBreaker(
    private val producer: KafkaRdfParseEventProducer,
    private val conceptHandler: ConceptHandler,
    private val harvestEventProducer: KafkaHarvestEventProducer,
    private val dataServiceHandler: DataServiceHandler,
    private val datasetHandler: DatasetHandler,
    private val eventHandler: EventHandler,
    private val informationModelHandler: InformationModelHandler,
    private val serviceHandler: ServiceHandler,
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
) {
    fun processConceptGeneric(event: GenericRecord) =
        executeWithCircuitBreaker("rdf-parse-concept") {
            processGeneric(event, ConceptEventType.CONCEPT_REASONED.name, RdfParseResourceType.CONCEPT, "concept")
        }

    fun processDataServiceGeneric(event: GenericRecord) =
        executeWithCircuitBreaker("rdf-parse-data-service") {
            processGeneric(event, DataServiceEventType.DATA_SERVICE_REASONED.name, RdfParseResourceType.DATA_SERVICE, "data service")
        }

    fun processDatasetGeneric(event: GenericRecord) =
        executeWithCircuitBreaker("rdf-parse-dataset") {
            processGeneric(event, DatasetEventType.DATASET_REASONED.name, RdfParseResourceType.DATASET, "dataset")
        }

    fun processInformationModelGeneric(event: GenericRecord) =
        executeWithCircuitBreaker("rdf-parse-information-model") {
            processGeneric(
                event,
                InformationModelEventType.INFORMATION_MODEL_REASONED.name,
                RdfParseResourceType.INFORMATION_MODEL,
                "information model",
            )
        }

    fun processServiceGeneric(event: GenericRecord) =
        executeWithCircuitBreaker("rdf-parse-service") {
            processGeneric(event, ServiceEventType.SERVICE_REASONED.name, RdfParseResourceType.SERVICE, "service")
        }

    fun processEventGeneric(event: GenericRecord) =
        executeWithCircuitBreaker("rdf-parse-event") {
            processGeneric(event, EventEventType.EVENT_REASONED.name, RdfParseResourceType.EVENT, "event")
        }

    fun processConcept(event: ConceptEvent) {
        executeWithCircuitBreaker("rdf-parse-concept") {
            val type = runCatching { event.type }.getOrNull()
            if (type == ConceptEventType.CONCEPT_REASONED) {
                val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
                val graph = runCatching { event.graph.toString() }.getOrNull()
                val timestamp = runCatching { event.timestamp }.getOrNull()
                val harvestRunId = runCatching { event.harvestRunId?.toString() }.getOrNull()
                val uri = runCatching { event.uri?.toString() }.getOrNull()

                if (fdkId != null && graph != null && timestamp != null) {
                    handleRecord(fdkId, graph, timestamp, RdfParseResourceType.CONCEPT, harvestRunId, uri)
                } else {
                    val graphForLog = graph?.let { truncateForLog(it, MAX_GRAPH_LOG_LENGTH) }
                    LOGGER.warn(
                        "Ignoring concept message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                        fdkId,
                        graphForLog,
                        timestamp,
                    )
                }
            }
        }
    }

    fun processDataService(event: DataServiceEvent) {
        executeWithCircuitBreaker("rdf-parse-data-service") {
            val type = runCatching { event.type }.getOrNull()
            if (type == DataServiceEventType.DATA_SERVICE_REASONED) {
                val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
                val graph = runCatching { event.graph.toString() }.getOrNull()
                val timestamp = runCatching { event.timestamp }.getOrNull()
                val harvestRunId = runCatching { event.harvestRunId?.toString() }.getOrNull()
                val uri = runCatching { event.uri?.toString() }.getOrNull()

                if (fdkId != null && graph != null && timestamp != null) {
                    handleRecord(fdkId, graph, timestamp, RdfParseResourceType.DATA_SERVICE, harvestRunId, uri)
                } else {
                    val graphForLog = graph?.let { truncateForLog(it, MAX_GRAPH_LOG_LENGTH) }
                    LOGGER.warn(
                        "Ignoring data service message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                        fdkId,
                        graphForLog,
                        timestamp,
                    )
                }
            }
        }
    }

    fun processDataset(event: DatasetEvent) {
        executeWithCircuitBreaker("rdf-parse-dataset") {
            val type = runCatching { event.type }.getOrNull()
            if (type == DatasetEventType.DATASET_REASONED) {
                val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
                val graph = runCatching { event.graph.toString() }.getOrNull()
                val timestamp = runCatching { event.timestamp }.getOrNull()
                val harvestRunId = runCatching { event.harvestRunId?.toString() }.getOrNull()
                val uri = runCatching { event.uri?.toString() }.getOrNull()

                if (fdkId != null && graph != null && timestamp != null) {
                    handleRecord(fdkId, graph, timestamp, RdfParseResourceType.DATASET, harvestRunId, uri)
                } else {
                    val graphForLog = graph?.let { truncateForLog(it, MAX_GRAPH_LOG_LENGTH) }
                    LOGGER.warn(
                        "Ignoring dataset message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                        fdkId,
                        graphForLog,
                        timestamp,
                    )
                }
            }
        }
    }

    fun processInformationModel(event: InformationModelEvent) {
        executeWithCircuitBreaker("rdf-parse-information-model") {
            val type = runCatching { event.type }.getOrNull()
            if (type == InformationModelEventType.INFORMATION_MODEL_REASONED) {
                val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
                val graph = runCatching { event.graph.toString() }.getOrNull()
                val timestamp = runCatching { event.timestamp }.getOrNull()
                val harvestRunId = runCatching { event.harvestRunId?.toString() }.getOrNull()
                val uri = runCatching { event.uri?.toString() }.getOrNull()

                if (fdkId != null && graph != null && timestamp != null) {
                    handleRecord(fdkId, graph, timestamp, RdfParseResourceType.INFORMATION_MODEL, harvestRunId, uri)
                } else {
                    val graphForLog = graph?.let { truncateForLog(it, MAX_GRAPH_LOG_LENGTH) }
                    LOGGER.warn(
                        "Ignoring information model message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                        fdkId,
                        graphForLog,
                        timestamp,
                    )
                }
            }
        }
    }

    fun processService(event: ServiceEvent) {
        executeWithCircuitBreaker("rdf-parse-service") {
            val type = runCatching { event.type }.getOrNull()
            if (type == ServiceEventType.SERVICE_REASONED) {
                val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
                val graph = runCatching { event.graph.toString() }.getOrNull()
                val timestamp = runCatching { event.timestamp }.getOrNull()
                val harvestRunId = runCatching { event.harvestRunId?.toString() }.getOrNull()
                val uri = runCatching { event.uri?.toString() }.getOrNull()

                if (fdkId != null && graph != null && timestamp != null) {
                    handleRecord(fdkId, graph, timestamp, RdfParseResourceType.SERVICE, harvestRunId, uri)
                } else {
                    val graphForLog = graph?.let { truncateForLog(it, MAX_GRAPH_LOG_LENGTH) }
                    LOGGER.warn(
                        "Ignoring service message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                        fdkId,
                        graphForLog,
                        timestamp,
                    )
                }
            }
        }
    }

    fun processEvent(event: EventEvent) {
        executeWithCircuitBreaker("rdf-parse-event") {
            val type = runCatching { event.type }.getOrNull()
            if (type == EventEventType.EVENT_REASONED) {
                val fdkId = runCatching { event.fdkId.toString() }.getOrNull()
                val graph = runCatching { event.graph.toString() }.getOrNull()
                val timestamp = runCatching { event.timestamp }.getOrNull()
                val harvestRunId = runCatching { event.harvestRunId?.toString() }.getOrNull()
                val uri = runCatching { event.uri?.toString() }.getOrNull()

                if (fdkId != null && graph != null && timestamp != null) {
                    handleRecord(fdkId, graph, timestamp, RdfParseResourceType.EVENT, harvestRunId, uri)
                } else {
                    val graphForLog = graph?.let { truncateForLog(it, MAX_GRAPH_LOG_LENGTH) }
                    LOGGER.warn(
                        "Ignoring event message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                        fdkId,
                        graphForLog,
                        timestamp,
                    )
                }
            }
        }
    }

    private fun executeWithCircuitBreaker(
        circuitBreakerName: String,
        block: () -> Unit,
    ) {
        circuitBreakerRegistry
            .circuitBreaker(circuitBreakerName)
            .executeRunnable(block)
    }

    private fun processGeneric(
        event: GenericRecord,
        expectedType: String,
        resourceType: RdfParseResourceType,
        resourceLabel: String,
    ) {
        val type = runCatching { event.get("type")?.toString() }.getOrNull()
        val fdkId = runCatching { event.get("fdkId")?.toString() }.getOrNull()
        val graph = runCatching { event.get("graph")?.toString() }.getOrNull()
        val timestamp =
            runCatching {
                when (val t = event.get("timestamp")) {
                    is Number -> t.toLong()
                    else -> null
                }
            }.getOrNull()
        val harvestRunId = runCatching { event.get("harvestRunId")?.toString() }.getOrNull()
        val uri = runCatching { event.get("uri")?.toString() }.getOrNull()

        if (type == expectedType && fdkId != null && graph != null && timestamp != null) {
            handleRecord(fdkId, graph, timestamp, resourceType, harvestRunId, uri)
            return
        }

        val graphForLog = graph?.let { truncateForLog(it, MAX_GRAPH_LOG_LENGTH) }
        val reason =
            if (type != expectedType) {
                "event type not $expectedType (got: $type)"
            } else {
                "missing required fields"
            }
        LOGGER.warn(
            "Ignoring generic {} message: {}. fdkId: {}, graph: {}, timestamp: {}, type: {}",
            resourceLabel,
            reason,
            fdkId,
            graphForLog,
            timestamp,
            type,
        )
    }

    private fun handleRecord(
        fdkId: String,
        graph: String,
        timestamp: Long,
        resourceType: RdfParseResourceType,
        harvestRunId: String?,
        uri: String?,
    ) {
        val startTime = Instant.now().toString()
        try {
            parseAndProduce(fdkId, graph, timestamp, resourceType, harvestRunId, uri)
            // Produce harvest event on success
            produceHarvestEvent(
                harvestRunId = harvestRunId,
                resourceType = resourceType,
                fdkId = fdkId,
                uri = uri,
                startTime = startTime,
                endTime = Instant.now().toString(),
                errorMessage = null,
            )
        } catch (e: RecoverableParseException) {
            LOGGER.warn("Recoverable parsing error: " + e.message)
            Metrics
                .counter(
                    "rdf_parse_error",
                    "type",
                    resourceType.toString().lowercase(),
                ).increment()
            // Produce harvest event on failure
            produceHarvestEvent(
                harvestRunId = harvestRunId,
                resourceType = resourceType,
                fdkId = fdkId,
                uri = uri,
                startTime = startTime,
                endTime = Instant.now().toString(),
                errorMessage = e.message,
            )
            throw e
        } catch (e: UnrecoverableParseException) {
            LOGGER.error("Unrecoverable parsing error: " + e.message)
            Metrics
                .counter(
                    "rdf_parse_error",
                    "type",
                    resourceType.toString().lowercase(),
                ).increment()
            // Produce harvest event on failure
            produceHarvestEvent(
                harvestRunId = harvestRunId,
                resourceType = resourceType,
                fdkId = fdkId,
                uri = uri,
                startTime = startTime,
                endTime = Instant.now().toString(),
                errorMessage = e.message,
            )
            throw e
        }
    }

    private fun parseAndProduce(
        fdkId: String,
        graph: String,
        timestamp: Long,
        type: RdfParseResourceType,
        harvestRunId: String?,
        uri: String?,
    ) {
        val timeElapsed =
            measureTimedValue {
                LOGGER.debug("Parse ${type.toString().lowercase()} - id: $fdkId")
                val json =
                    when (type) {
                        RdfParseResourceType.CONCEPT -> conceptHandler.parseConcept(fdkId, graph)
                        RdfParseResourceType.DATA_SERVICE -> dataServiceHandler.parseDataService(fdkId, graph)
                        RdfParseResourceType.DATASET -> datasetHandler.parseDataset(fdkId, graph)
                        RdfParseResourceType.EVENT -> eventHandler.parseEvent(fdkId, graph)
                        RdfParseResourceType.INFORMATION_MODEL -> informationModelHandler.parseInformationModel(fdkId, graph)
                        RdfParseResourceType.SERVICE -> serviceHandler.parseService(fdkId, graph)
                    }
                val rdfParseEvent =
                    RdfParseEvent
                        .newBuilder()
                        .setResourceType(type)
                        .setHarvestRunId(harvestRunId)
                        .setUri(uri)
                        .setFdkId(fdkId)
                        .setData(json.toString())
                        .setTimestamp(timestamp)
                        .build()
                producer.sendMessage(rdfParseEvent)
            }
        Metrics
            .timer(
                "rdf_parse",
                "type",
                type.toString().lowercase(),
            ).record(timeElapsed.duration.toJavaDuration())
    }

    private fun mapResourceTypeToDataType(resourceType: RdfParseResourceType): DataType =
        when (resourceType) {
            RdfParseResourceType.DATASET -> DataType.dataset
            RdfParseResourceType.DATA_SERVICE -> DataType.dataservice
            RdfParseResourceType.INFORMATION_MODEL -> DataType.informationmodel
            RdfParseResourceType.CONCEPT -> DataType.concept
            RdfParseResourceType.SERVICE -> DataType.publicService
            RdfParseResourceType.EVENT -> DataType.event
        }

    private fun produceHarvestEvent(
        harvestRunId: String?,
        resourceType: RdfParseResourceType,
        fdkId: String,
        uri: String?,
        startTime: String,
        endTime: String,
        errorMessage: String?,
    ) {
        if (harvestRunId.isNullOrBlank()) return
        val harvestEvent =
            HarvestEvent(
                HarvestPhase.RDF_PARSING,
                harvestRunId,
                mapResourceTypeToDataType(resourceType),
                null,
                null,
                null,
                fdkId,
                uri,
                startTime,
                endTime,
                errorMessage,
                null,
                null,
                null,
                false,
            )
        harvestEventProducer.sendMessage(harvestEvent)
    }

    companion object {
        private const val MAX_GRAPH_LOG_LENGTH = 200
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaReasonedEventCircuitBreaker::class.java)

        private fun truncateForLog(
            s: String,
            maxLength: Int,
        ): String = if (s.length <= maxLength) s else s.take(maxLength) + "... (${s.length} chars total)"
    }
}
