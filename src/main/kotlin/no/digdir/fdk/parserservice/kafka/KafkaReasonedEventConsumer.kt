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
        LOGGER.debug("Received data service message - offset: " + record.offset())
        try {
            when (val message = runCatching { record.value() }.getOrNull()) {
                is SpecificRecord -> {
                    val dataServiceEvent =
                        try {
                            message as DataServiceEvent
                        } catch (ex: Exception) {
                            LOGGER.error("Error parsing data service message", ex)
                            throw UnrecoverableParseException("Error parsing data service message")
                        }
                    circuitBreaker.processDataService(dataServiceEvent)
                }
                is GenericRecord -> circuitBreaker.processGeneric(message)
                else -> LOGGER.warn("Unknown message type: {}", message?.javaClass)
            }
            ack.acknowledge()
        } catch (e: RecoverableParseException) {
            ack.acknowledge()
        } catch (e: UnrecoverableParseException) {
            ack.nack(Duration.ZERO)
        }
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
        LOGGER.debug("Received concept message - offset: " + record.offset())
        try {
            when (val message = runCatching { record.value() }.getOrNull()) {
                is SpecificRecord -> {
                    val conceptEvent =
                        try {
                            message as ConceptEvent
                        } catch (ex: Exception) {
                            LOGGER.error("Error parsing concept message", ex)
                            throw UnrecoverableParseException("Error parsing concept message")
                        }
                    circuitBreaker.processConcept(conceptEvent)
                }
                is GenericRecord -> circuitBreaker.processGeneric(message)
                else -> LOGGER.warn("Unknown message type: {}", message?.javaClass)
            }
            ack.acknowledge()
        } catch (e: RecoverableParseException) {
            ack.acknowledge()
        } catch (e: UnrecoverableParseException) {
            ack.nack(Duration.ZERO)
        }
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
        LOGGER.debug("Received dataset message - offset: " + record.offset())
        try {
            when (val message = runCatching { record.value() }.getOrNull()) {
                is SpecificRecord -> {
                    val datasetEvent =
                        try {
                            message as DatasetEvent
                        } catch (ex: Exception) {
                            LOGGER.error("Error parsing dataset message", ex)
                            throw UnrecoverableParseException("Error parsing dataset message")
                        }
                    circuitBreaker.processDataset(datasetEvent)
                }
                is GenericRecord -> circuitBreaker.processGeneric(message)
                else -> LOGGER.warn("Unknown message type: {}", message?.javaClass)
            }
            ack.acknowledge()
        } catch (e: RecoverableParseException) {
            ack.acknowledge()
        } catch (e: UnrecoverableParseException) {
            ack.nack(Duration.ZERO)
        }
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
        LOGGER.debug("Received information model message - offset: " + record.offset())
        try {
            when (val message = runCatching { record.value() }.getOrNull()) {
                is SpecificRecord -> {
                    val infoModelEvent =
                        try {
                            message as InformationModelEvent
                        } catch (ex: Exception) {
                            LOGGER.error("Error parsing information model message", ex)
                            throw UnrecoverableParseException("Error parsing information model message")
                        }
                    circuitBreaker.processInformationModel(infoModelEvent)
                }
                is GenericRecord -> circuitBreaker.processGeneric(message)
                else -> LOGGER.warn("Unknown message type: {}", message?.javaClass)
            }
            ack.acknowledge()
        } catch (e: RecoverableParseException) {
            ack.acknowledge()
        } catch (e: UnrecoverableParseException) {
            ack.nack(Duration.ZERO)
        }
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
        LOGGER.debug("Received service message - offset: " + record.offset())
        try {
            when (val message = runCatching { record.value() }.getOrNull()) {
                is SpecificRecord -> {
                    val serviceEvent =
                        try {
                            message as ServiceEvent
                        } catch (ex: Exception) {
                            LOGGER.error("Error parsing service message", ex)
                            throw UnrecoverableParseException("Error parsing service message")
                        }
                    circuitBreaker.processService(serviceEvent)
                }
                is GenericRecord -> circuitBreaker.processGeneric(message)
                else -> LOGGER.warn("Unknown message type: {}", message?.javaClass)
            }
            ack.acknowledge()
        } catch (e: RecoverableParseException) {
            ack.acknowledge()
        } catch (e: UnrecoverableParseException) {
            ack.nack(Duration.ZERO)
        }
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
        LOGGER.debug("Received event message - offset: " + record.offset())
        try {
            when (val message = runCatching { record.value() }.getOrNull()) {
                is SpecificRecord -> {
                    val eventEvent =
                        try {
                            message as EventEvent
                        } catch (ex: Exception) {
                            LOGGER.error("Error parsing event message", ex)
                            throw UnrecoverableParseException("Error parsing event message")
                        }
                    circuitBreaker.processEvent(eventEvent)
                }
                is GenericRecord -> circuitBreaker.processGeneric(message)
                else -> LOGGER.warn("Unknown message type: {}", message?.javaClass)
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
