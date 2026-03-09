package no.digdir.fdk.parserservice.kafka

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
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
open class KafkaReasonedEventCircuitBreaker(
    private val producer: KafkaRdfParseEventProducer,
    private val conceptHandler: ConceptHandler,
    private val harvestEventProducer: KafkaHarvestEventProducer,
    private val dataServiceHandler: DataServiceHandler,
    private val datasetHandler: DatasetHandler,
    private val eventHandler: EventHandler,
    private val informationModelHandler: InformationModelHandler,
    private val serviceHandler: ServiceHandler,
) {
    @CircuitBreaker(name = "rdf-parse-generic")
    open fun processGeneric(event: GenericRecord) {
        val type = runCatching { event.get("type")?.toString() }.getOrNull()
        val resourceType =
            when (type) {
                ConceptEventType.CONCEPT_REASONED.name -> RdfParseResourceType.CONCEPT
                DataServiceEventType.DATA_SERVICE_REASONED.name -> RdfParseResourceType.DATA_SERVICE
                DatasetEventType.DATASET_REASONED.name -> RdfParseResourceType.DATASET
                EventEventType.EVENT_REASONED.name -> RdfParseResourceType.EVENT
                InformationModelEventType.INFORMATION_MODEL_REASONED.name -> RdfParseResourceType.INFORMATION_MODEL
                ServiceEventType.SERVICE_REASONED.name -> RdfParseResourceType.SERVICE
                else -> null
            }

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

        if (fdkId != null && graph != null && timestamp != null && resourceType != null) {
            handleRecord(fdkId, graph, timestamp, resourceType, harvestRunId, uri)
        } else {
            val recordFields =
                runCatching {
                    event.schema?.fields?.associate { field ->
                        val value = event.get(field.name())
                        val display =
                            when {
                                field.name().equals("graph", ignoreCase = true) && value != null ->
                                    truncateForLog(value.toString(), MAX_GRAPH_LOG_LENGTH)
                                else -> value
                            }
                        field.name() to display
                    } ?: emptyMap()
                }.getOrElse { emptyMap() }
            val reason =
                if (resourceType == null) {
                    "event type not REASONED (got: $type)"
                } else {
                    "missing required fields"
                }
            val graphForLog = graph?.let { truncateForLog(it, MAX_GRAPH_LOG_LENGTH) }
            LOGGER.warn(
                "Ignoring message: {}. fdkId: {}, graph: {}, timestamp: {}, type: {}. GenericRecord: {}",
                reason,
                fdkId,
                graphForLog,
                timestamp,
                type,
                recordFields,
            )
        }
    }

    @CircuitBreaker(name = "rdf-parse-concept")
    open fun processConcept(event: ConceptEvent) {
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
                LOGGER.warn(
                    "Ignoring concept message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                    fdkId,
                    graph,
                    timestamp,
                )
            }
        }
    }

    @CircuitBreaker(name = "rdf-parse-data-service")
    open fun processDataService(event: DataServiceEvent) {
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
                LOGGER.warn(
                    "Ignoring data service message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                    fdkId,
                    graph,
                    timestamp,
                )
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
            val harvestRunId = runCatching { event.harvestRunId?.toString() }.getOrNull()
            val uri = runCatching { event.uri?.toString() }.getOrNull()

            if (fdkId != null && graph != null && timestamp != null) {
                handleRecord(fdkId, graph, timestamp, RdfParseResourceType.DATASET, harvestRunId, uri)
            } else {
                LOGGER.warn(
                    "Ignoring dataset message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                    fdkId,
                    graph,
                    timestamp,
                )
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
            val harvestRunId = runCatching { event.harvestRunId?.toString() }.getOrNull()
            val uri = runCatching { event.uri?.toString() }.getOrNull()

            if (fdkId != null && graph != null && timestamp != null) {
                handleRecord(fdkId, graph, timestamp, RdfParseResourceType.INFORMATION_MODEL, harvestRunId, uri)
            } else {
                LOGGER.warn(
                    "Ignoring information model message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                    fdkId,
                    graph,
                    timestamp,
                )
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
            val harvestRunId = runCatching { event.harvestRunId?.toString() }.getOrNull()
            val uri = runCatching { event.uri?.toString() }.getOrNull()

            if (fdkId != null && graph != null && timestamp != null) {
                handleRecord(fdkId, graph, timestamp, RdfParseResourceType.SERVICE, harvestRunId, uri)
            } else {
                LOGGER.warn(
                    "Ignoring service message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                    fdkId,
                    graph,
                    timestamp,
                )
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
            val harvestRunId = runCatching { event.harvestRunId?.toString() }.getOrNull()
            val uri = runCatching { event.uri?.toString() }.getOrNull()

            if (fdkId != null && graph != null && timestamp != null) {
                handleRecord(fdkId, graph, timestamp, RdfParseResourceType.EVENT, harvestRunId, uri)
            } else {
                LOGGER.warn(
                    "Ignoring event message with missing required fields. fdkId: {}, graph: {}, timestamp: {}",
                    fdkId,
                    graph,
                    timestamp,
                )
            }
        }
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
