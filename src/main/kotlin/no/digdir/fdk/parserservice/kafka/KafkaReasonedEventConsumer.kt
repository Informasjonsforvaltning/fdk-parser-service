package no.digdir.fdk.parserservice.kafka

import no.digdir.fdk.parserservice.model.RecoverableParseException
import no.digdir.fdk.parserservice.model.UnrecoverableParseException
import no.fdk.concept.ConceptEvent
import no.fdk.dataservice.DataServiceEvent
import no.fdk.dataset.DatasetEvent
import no.fdk.event.EventEvent
import no.fdk.informationmodel.InformationModelEvent
import no.fdk.service.ServiceEvent
import org.apache.avro.generic.GenericRecord
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class KafkaReasonedEventConsumer(
    private val circuitBreaker: KafkaReasonedEventCircuitBreaker,
) {
    @KafkaListener(
        topics = ["data-service-events"],
        groupId = "fdk-parser-service",
        concurrency = "4",
        containerFactory = "kafkaListenerContainerFactory",
        id = "data-service-event-consumer",
    )
    fun dataServiceListener(
        record: ConsumerRecord<String, Any>,
        ack: Acknowledgment,
    ) {
        handleListenerMessage<DataServiceEvent>(
            record = record,
            ack = ack,
            resourceLabel = "data service",
            processSpecific = circuitBreaker::processDataService,
            processGeneric = circuitBreaker::processDataServiceGeneric,
        )
    }

    @KafkaListener(
        topics = ["concept-events"],
        groupId = "fdk-parser-service",
        concurrency = "4",
        containerFactory = "kafkaListenerContainerFactory",
        id = "concept-event-consumer",
    )
    fun conceptListener(
        record: ConsumerRecord<String, Any>,
        ack: Acknowledgment,
    ) {
        handleListenerMessage<ConceptEvent>(
            record = record,
            ack = ack,
            resourceLabel = "concept",
            processSpecific = circuitBreaker::processConcept,
            processGeneric = circuitBreaker::processConceptGeneric,
        )
    }

    @KafkaListener(
        topics = ["dataset-events"],
        groupId = "fdk-parser-service",
        concurrency = "4",
        containerFactory = "kafkaListenerContainerFactory",
        id = "dataset-event-consumer",
    )
    fun datasetListener(
        record: ConsumerRecord<String, Any>,
        ack: Acknowledgment,
    ) {
        handleListenerMessage<DatasetEvent>(
            record = record,
            ack = ack,
            resourceLabel = "dataset",
            processSpecific = circuitBreaker::processDataset,
            processGeneric = circuitBreaker::processDatasetGeneric,
        )
    }

    @KafkaListener(
        topics = ["information-model-events"],
        groupId = "fdk-parser-service",
        concurrency = "4",
        containerFactory = "kafkaListenerContainerFactory",
        id = "information-model-event-consumer",
    )
    fun informationModelListener(
        record: ConsumerRecord<String, Any>,
        ack: Acknowledgment,
    ) {
        handleListenerMessage<InformationModelEvent>(
            record = record,
            ack = ack,
            resourceLabel = "information model",
            processSpecific = circuitBreaker::processInformationModel,
            processGeneric = circuitBreaker::processInformationModelGeneric,
        )
    }

    @KafkaListener(
        topics = ["service-events"],
        groupId = "fdk-parser-service",
        concurrency = "4",
        containerFactory = "kafkaListenerContainerFactory",
        id = "service-event-consumer",
    )
    fun serviceListener(
        record: ConsumerRecord<String, Any>,
        ack: Acknowledgment,
    ) {
        handleListenerMessage<ServiceEvent>(
            record = record,
            ack = ack,
            resourceLabel = "service",
            processSpecific = circuitBreaker::processService,
            processGeneric = circuitBreaker::processServiceGeneric,
        )
    }

    @KafkaListener(
        topics = ["event-events"],
        groupId = "fdk-parser-service",
        concurrency = "4",
        containerFactory = "kafkaListenerContainerFactory",
        id = "event-event-consumer",
    )
    fun eventListener(
        record: ConsumerRecord<String, Any>,
        ack: Acknowledgment,
    ) {
        handleListenerMessage<EventEvent>(
            record = record,
            ack = ack,
            resourceLabel = "event",
            processSpecific = circuitBreaker::processEvent,
            processGeneric = circuitBreaker::processEventGeneric,
        )
    }

    private inline fun <reified T : SpecificRecord> handleListenerMessage(
        record: ConsumerRecord<String, Any>,
        ack: Acknowledgment,
        resourceLabel: String,
        processSpecific: (T) -> Unit,
        processGeneric: (GenericRecord) -> Unit,
    ) {
        LOGGER.debug("Received $resourceLabel message - offset: " + record.offset())
        try {
            when (val message = runCatching { record.value() }.getOrNull()) {
                is SpecificRecord -> {
                    val typedEvent =
                        try {
                            message as T
                        } catch (ex: Exception) {
                            LOGGER.error("Error parsing $resourceLabel message", ex)
                            throw UnrecoverableParseException("Error parsing $resourceLabel message")
                        }
                    processSpecific(typedEvent)
                }

                is GenericRecord -> {
                    processGeneric(message)
                }

                else -> {
                    LOGGER.warn("Unknown message type: {}", message?.javaClass)
                }
            }
            ack.acknowledge()
        } catch (e: RecoverableParseException) {
            ack.acknowledge()
        } catch (e: UnrecoverableParseException) {
            ack.nack(Duration.ZERO)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(KafkaReasonedEventConsumer::class.java)
    }
}
