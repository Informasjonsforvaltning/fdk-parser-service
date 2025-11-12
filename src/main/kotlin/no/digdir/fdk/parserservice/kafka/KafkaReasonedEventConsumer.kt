package no.digdir.fdk.parserservice.kafka

import no.digdir.fdk.parserservice.model.RecoverableParseException
import no.digdir.fdk.parserservice.model.UnrecoverableParseException
import no.fdk.dataservice.DataServiceEvent
import no.fdk.dataset.DatasetEvent
import no.fdk.informationmodel.InformationModelEvent
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
        record: ConsumerRecord<String, Object>,
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
                else -> LOGGER.warn("Unknown message type: {}", message?.getClass())
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
        record: ConsumerRecord<String, Object>,
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
                else -> LOGGER.warn("Unknown message type: {}", message?.getClass())
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
        record: ConsumerRecord<String, Object>,
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
                else -> LOGGER.warn("Unknown message type: {}", message?.getClass())
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
