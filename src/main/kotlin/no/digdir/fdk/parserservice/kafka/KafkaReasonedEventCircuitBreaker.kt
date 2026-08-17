package no.digdir.fdk.parserservice.kafka

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import no.digdir.fdk.parserservice.handler.ConceptHandler
import no.digdir.fdk.parserservice.handler.DataServiceHandler
import no.digdir.fdk.parserservice.handler.DatasetHandler
import no.digdir.fdk.parserservice.handler.EventHandler
import no.digdir.fdk.parserservice.handler.InformationModelHandler
import no.digdir.fdk.parserservice.handler.ServiceHandler
import no.digdir.fdk.parserservice.metrics.ParseMetrics
import no.digdir.fdk.parserservice.model.NoParserMatchedException
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
    fun processConceptGeneric(event: GenericRecord): ProcessOutcome = executeWithCircuitBreaker("rdf-parse-concept") {
        processGeneric(event, ConceptEventType.CONCEPT_REASONED.name, RdfParseResourceType.CONCEPT, "concept")
    }

    fun processDataServiceGeneric(event: GenericRecord): ProcessOutcome = executeWithCircuitBreaker("rdf-parse-data-service") {
        processGeneric(event, DataServiceEventType.DATA_SERVICE_REASONED.name, RdfParseResourceType.DATA_SERVICE, "data service")
    }

    fun processDatasetGeneric(event: GenericRecord): ProcessOutcome = executeWithCircuitBreaker("rdf-parse-dataset") {
        processGeneric(event, DatasetEventType.DATASET_REASONED.name, RdfParseResourceType.DATASET, "dataset")
    }

    fun processInformationModelGeneric(event: GenericRecord): ProcessOutcome = executeWithCircuitBreaker("rdf-parse-information-model") {
        processGeneric(
            event,
            InformationModelEventType.INFORMATION_MODEL_REASONED.name,
            RdfParseResourceType.INFORMATION_MODEL,
            "information model",
        )
    }

    fun processServiceGeneric(event: GenericRecord): ProcessOutcome = executeWithCircuitBreaker("rdf-parse-service") {
        processGeneric(event, ServiceEventType.SERVICE_REASONED.name, RdfParseResourceType.SERVICE, "service")
    }

    fun processEventGeneric(event: GenericRecord): ProcessOutcome = executeWithCircuitBreaker("rdf-parse-event") {
        processGeneric(event, EventEventType.EVENT_REASONED.name, RdfParseResourceType.EVENT, "event")
    }

    fun processConcept(event: ConceptEvent): ProcessOutcome = processTypedEvent(
        circuitBreakerName = "rdf-parse-concept",
        resourceType = RdfParseResourceType.CONCEPT,
        resourceLabel = "concept",
        event = event,
        isExpectedType = { runCatching { it.type }.getOrNull() == ConceptEventType.CONCEPT_REASONED },
        extractFields = {
            extractReasonedEventFields(
                fdkId = { it.fdkId },
                graph = { it.graph },
                timestamp = { it.timestamp },
                harvestRunId = { it.harvestRunId },
                uri = { it.uri },
                catalogGraph = { it.catalogGraph },
            )
        },
    )

    fun processDataService(event: DataServiceEvent): ProcessOutcome = processTypedEvent(
        circuitBreakerName = "rdf-parse-data-service",
        resourceType = RdfParseResourceType.DATA_SERVICE,
        resourceLabel = "data service",
        event = event,
        isExpectedType = { runCatching { it.type }.getOrNull() == DataServiceEventType.DATA_SERVICE_REASONED },
        extractFields = {
            extractReasonedEventFields(
                fdkId = { it.fdkId },
                graph = { it.graph },
                timestamp = { it.timestamp },
                harvestRunId = { it.harvestRunId },
                uri = { it.uri },
                catalogGraph = { it.catalogGraph },
            )
        },
    )

    fun processDataset(event: DatasetEvent): ProcessOutcome = processTypedEvent(
        circuitBreakerName = "rdf-parse-dataset",
        resourceType = RdfParseResourceType.DATASET,
        resourceLabel = "dataset",
        event = event,
        isExpectedType = { runCatching { it.type }.getOrNull() == DatasetEventType.DATASET_REASONED },
        extractFields = {
            extractReasonedEventFields(
                fdkId = { it.fdkId },
                graph = { it.graph },
                timestamp = { it.timestamp },
                harvestRunId = { it.harvestRunId },
                uri = { it.uri },
                catalogGraph = { it.catalogGraph },
            )
        },
    )

    fun processInformationModel(event: InformationModelEvent): ProcessOutcome = processTypedEvent(
        circuitBreakerName = "rdf-parse-information-model",
        resourceType = RdfParseResourceType.INFORMATION_MODEL,
        resourceLabel = "information model",
        event = event,
        isExpectedType = {
            runCatching { it.type }.getOrNull() == InformationModelEventType.INFORMATION_MODEL_REASONED
        },
        extractFields = {
            extractReasonedEventFields(
                fdkId = { it.fdkId },
                graph = { it.graph },
                timestamp = { it.timestamp },
                harvestRunId = { it.harvestRunId },
                uri = { it.uri },
                catalogGraph = { it.catalogGraph },
            )
        },
    )

    fun processService(event: ServiceEvent): ProcessOutcome = processTypedEvent(
        circuitBreakerName = "rdf-parse-service",
        resourceType = RdfParseResourceType.SERVICE,
        resourceLabel = "service",
        event = event,
        isExpectedType = { runCatching { it.type }.getOrNull() == ServiceEventType.SERVICE_REASONED },
        extractFields = {
            extractReasonedEventFields(
                fdkId = { it.fdkId },
                graph = { it.graph },
                timestamp = { it.timestamp },
                harvestRunId = { it.harvestRunId },
                uri = { it.uri },
                catalogGraph = { it.catalogGraph },
            )
        },
    )

    fun processEvent(event: EventEvent): ProcessOutcome = processTypedEvent(
        circuitBreakerName = "rdf-parse-event",
        resourceType = RdfParseResourceType.EVENT,
        resourceLabel = "event",
        event = event,
        isExpectedType = { runCatching { it.type }.getOrNull() == EventEventType.EVENT_REASONED },
        extractFields = {
            extractReasonedEventFields(
                fdkId = { it.fdkId },
                graph = { it.graph },
                timestamp = { it.timestamp },
                harvestRunId = { it.harvestRunId },
                uri = { it.uri },
                catalogGraph = { it.catalogGraph },
            )
        },
    )

    private inline fun <T> processTypedEvent(
        circuitBreakerName: String,
        resourceType: RdfParseResourceType,
        resourceLabel: String,
        event: T,
        crossinline isExpectedType: (T) -> Boolean,
        crossinline extractFields: (T) -> ReasonedEventFields,
    ): ProcessOutcome = executeWithCircuitBreaker(circuitBreakerName) {
        if (!isExpectedType(event)) {
            return@executeWithCircuitBreaker ProcessOutcome.Skipped
        }

        val fields = extractFields(event)
        if (fields.fdkId != null && fields.graph != null && fields.timestamp != null) {
            handleRecord(
                fdkId = fields.fdkId,
                graph = fields.graph,
                timestamp = fields.timestamp,
                resourceType = resourceType,
                harvestRunId = fields.harvestRunId,
                uri = fields.uri,
                catalogGraph = fields.catalogGraph,
            )
            ProcessOutcome.Success(resourceType)
        } else {
            val graphForLog = fields.graph?.let { truncateForLog(it, MAX_GRAPH_LOG_LENGTH) }
            LOGGER.warn(
                "Ignoring $resourceLabel message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                fields.fdkId,
                graphForLog,
                fields.timestamp,
            )
            ProcessOutcome.Skipped
        }
    }

    private fun extractReasonedEventFields(
        fdkId: () -> Any?,
        graph: () -> Any?,
        timestamp: () -> Long?,
        harvestRunId: () -> Any?,
        uri: () -> Any?,
        catalogGraph: () -> Any?,
    ): ReasonedEventFields = ReasonedEventFields(
        fdkId = runCatching { fdkId()?.toString() }.getOrNull(),
        graph = runCatching { graph()?.toString() }.getOrNull(),
        timestamp = runCatching { timestamp() }.getOrNull(),
        harvestRunId = runCatching { harvestRunId()?.toString() }.getOrNull(),
        uri = runCatching { uri()?.toString() }.getOrNull(),
        catalogGraph = runCatching { catalogGraph()?.toString() }.getOrNull(),
    )

    private data class ReasonedEventFields(
        val fdkId: String?,
        val graph: String?,
        val timestamp: Long?,
        val harvestRunId: String?,
        val uri: String?,
        val catalogGraph: String?,
    )

    private fun executeWithCircuitBreaker(circuitBreakerName: String, block: () -> ProcessOutcome): ProcessOutcome = circuitBreakerRegistry
        .circuitBreaker(circuitBreakerName)
        .executeSupplier(block)

    private fun processGeneric(
        event: GenericRecord,
        expectedType: String,
        resourceType: RdfParseResourceType,
        resourceLabel: String,
    ): ProcessOutcome {
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
        val catalogGraph = runCatching { event.get("catalogGraph")?.toString() }.getOrNull()

        if (type == expectedType && fdkId != null && graph != null && timestamp != null) {
            handleRecord(fdkId, graph, timestamp, resourceType, harvestRunId, uri, catalogGraph)
            return ProcessOutcome.Success(resourceType)
        }

        val graphForLog = graph?.let { truncateForLog(it, MAX_GRAPH_LOG_LENGTH) }
        if (type != expectedType) {
            LOGGER.debug(
                "Ignoring generic {} message: event type not {} (got: {}). fdkId: {}, graph: {}, timestamp: {}, type: {}",
                resourceLabel,
                expectedType,
                type,
                fdkId,
                graphForLog,
                timestamp,
                type,
            )
        } else {
            LOGGER.warn(
                "Ignoring generic {} message: missing required fields. fdkId: {}, graph: {}, timestamp: {}, type: {}",
                resourceLabel,
                fdkId,
                graphForLog,
                timestamp,
                type,
            )
        }
        return ProcessOutcome.Skipped
    }

    private fun handleRecord(
        fdkId: String,
        graph: String,
        timestamp: Long,
        resourceType: RdfParseResourceType,
        harvestRunId: String?,
        uri: String?,
        catalogGraph: String?,
    ) {
        val startTime = Instant.now().toString()
        ParseMetrics.recordPipelineLag(resourceType, System.currentTimeMillis() - timestamp)
        ParseMetrics.recordPayloadSize(resourceType, ParseMetrics.PayloadDirection.INPUT, graph.length)
        try {
            parseAndProduce(fdkId, graph, timestamp, resourceType, harvestRunId, uri, catalogGraph)
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
            handleParseFailure(e, resourceType, harvestRunId, fdkId, uri, startTime)
        } catch (e: UnrecoverableParseException) {
            handleParseFailure(e, resourceType, harvestRunId, fdkId, uri, startTime)
        } catch (e: IllegalStateException) {
            handleParseFailure(
                NoParserMatchedException(e.message ?: "No parser could parse the resource", e),
                resourceType,
                harvestRunId,
                fdkId,
                uri,
                startTime,
            )
        }
    }

    private fun handleParseFailure(
        e: Exception,
        resourceType: RdfParseResourceType,
        harvestRunId: String?,
        fdkId: String,
        uri: String?,
        startTime: String,
    ): Nothing {
        when (e) {
            is RecoverableParseException -> LOGGER.warn("Recoverable parsing error: " + e.message)
            is UnrecoverableParseException -> LOGGER.error("Unrecoverable parsing error: " + e.message)
        }
        ParseMetrics.recordError(resourceType, e)
        produceHarvestEvent(
            harvestRunId = harvestRunId,
            resourceType = resourceType,
            fdkId = fdkId,
            uri = uri,
            startTime = startTime,
            endTime = Instant.now().toString(),
            errorMessage = e.message,
        )
        throw when (e) {
            is RecoverableParseException -> RecoverableParseProcessingException(resourceType, e)
            is UnrecoverableParseException -> UnrecoverableParseProcessingException(resourceType, e)
            else -> e
        }
    }

    private fun parseAndProduce(
        fdkId: String,
        graph: String,
        timestamp: Long,
        type: RdfParseResourceType,
        harvestRunId: String?,
        uri: String?,
        catalogGraph: String?,
    ) {
        val timeElapsed =
            measureTimedValue {
                LOGGER.debug("Parse ${type.toString().lowercase()} - id: $fdkId")
                val json =
                    when (type) {
                        RdfParseResourceType.CONCEPT -> conceptHandler.parseConcept(fdkId, graph, catalogGraph)
                        RdfParseResourceType.DATA_SERVICE -> dataServiceHandler.parseDataService(fdkId, graph, catalogGraph)
                        RdfParseResourceType.DATASET -> datasetHandler.parseDataset(fdkId, graph, catalogGraph)
                        RdfParseResourceType.EVENT -> eventHandler.parseEvent(fdkId, graph, catalogGraph)
                        RdfParseResourceType.INFORMATION_MODEL -> informationModelHandler.parseInformationModel(fdkId, graph, catalogGraph)
                        RdfParseResourceType.SERVICE -> serviceHandler.parseService(fdkId, graph, catalogGraph)
                    }
                val data = json.toString()
                ParseMetrics.recordPayloadSize(type, ParseMetrics.PayloadDirection.OUTPUT, data.length)
                val rdfParseEvent =
                    RdfParseEvent
                        .newBuilder()
                        .setResourceType(type)
                        .setHarvestRunId(harvestRunId)
                        .setUri(uri)
                        .setFdkId(fdkId)
                        .setData(data)
                        .setTimestamp(timestamp)
                        .build()
                producer.sendMessage(rdfParseEvent)
            }
        ParseMetrics.recordTotal(type, timeElapsed.duration)
    }

    private fun mapResourceTypeToDataType(resourceType: RdfParseResourceType): DataType = when (resourceType) {
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

    sealed class ProcessOutcome {
        data object Skipped : ProcessOutcome()

        data class Success(val resourceType: RdfParseResourceType) : ProcessOutcome()
    }

    companion object {
        private const val MAX_GRAPH_LOG_LENGTH = 200
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaReasonedEventCircuitBreaker::class.java)

        private fun truncateForLog(s: String, maxLength: Int): String =
            if (s.length <= maxLength) s else s.take(maxLength) + "... (${s.length} chars total)"
    }
}

class RecoverableParseProcessingException(val resourceType: RdfParseResourceType, cause: Throwable) :
    RuntimeException(cause.message, cause)

class UnrecoverableParseProcessingException(val resourceType: RdfParseResourceType, cause: Throwable) :
    RuntimeException(cause.message, cause)
