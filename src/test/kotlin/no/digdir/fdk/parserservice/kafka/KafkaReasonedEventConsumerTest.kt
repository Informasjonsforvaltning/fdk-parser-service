package no.digdir.fdk.parserservice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.digdir.fdk.parserservice.model.RecoverableParseException
import no.digdir.fdk.parserservice.model.UnrecoverableParseException
import no.digdir.fdk.parserservice.handler.DatasetHandler
import no.fdk.dataset.DatasetEvent
import no.fdk.dataset.DatasetEventType
import no.fdk.rdf.parse.RdfParseEvent
import no.fdk.rdf.parse.RdfParseResourceType
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

@ActiveProfiles("test")
@Tag("unit")
class KafkaReasonedEventConsumerTest {
    private val datasetHandler: DatasetHandler = mockk()
    private val kafkaTemplate: KafkaTemplate<String, RdfParseEvent> = mockk()
    private val ack: Acknowledgment = mockk()
    private val kafkaRdfParseEventProducer = KafkaRdfParseEventProducer(kafkaTemplate)
    private val circuitBreaker = KafkaReasonedEventCircuitBreaker(kafkaRdfParseEventProducer, datasetHandler)
    private val kafkaReasonedEventConsumer = KafkaReasonedEventConsumer(circuitBreaker)
    private val mapper = ObjectMapper()

    @Test
    fun `listen should produce a rdf parse event`() {
        val parsedJson = "{\"data\":\"my-parsed-rdf\"}"
        every { datasetHandler.parseDataset(any(), any()) } returns mapper.readTree(parsedJson)
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent = DatasetEvent(DatasetEventType.DATASET_REASONED, "my-id", "uri", System.currentTimeMillis())
        kafkaReasonedEventConsumer.listen(
            record = ConsumerRecord("dataset-events", 0, 0, "my-id", datasetEvent as Object),
            ack = ack
        )

        verify {
            kafkaTemplate.send(withArg {
                assertEquals("rdf-parse-events", it)
            }, withArg {
                assertEquals(datasetEvent.fdkId, it.fdkId)
                assertEquals(RdfParseResourceType.DATASET, it.resourceType)
                assertEquals(parsedJson, it.data)
                assertEquals(datasetEvent.timestamp, it.timestamp)
            })
            ack.acknowledge()
        }
        confirmVerified(kafkaTemplate, ack)
    }

    @Test
    fun `listen should acknowledge when a recoverable exception occurs`() {
        every {
            datasetHandler.parseDataset(any(), any())
        } throws RecoverableParseException("Error parsing RDF: invalid rdf")
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent = DatasetEvent(DatasetEventType.DATASET_REASONED, "my-id", "uri", System.currentTimeMillis())
        kafkaReasonedEventConsumer.listen(
            record = ConsumerRecord("dataset-events", 0, 0, "my-id", datasetEvent as Object),
            ack = ack
        )

        verify(exactly = 0) { kafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { ack.acknowledge() }
        verify(exactly = 0) { ack.nack(Duration.ZERO) }
        confirmVerified(kafkaTemplate, ack)
    }

    @Test
    fun `listen should not acknowledge when a unrecoverable exception occurs`() {
        every { datasetHandler.parseDataset(any(), any()) } throws UnrecoverableParseException("Error parsing RDF")
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent = DatasetEvent(DatasetEventType.DATASET_REASONED, "my-id", "uri", System.currentTimeMillis())
        kafkaReasonedEventConsumer.listen(
            record = ConsumerRecord("dataset-events", 0, 0, "my-id", datasetEvent as Object),
            ack = ack
        )

        verify(exactly = 0) { kafkaTemplate.send(any(), any()) }
        verify(exactly = 0) { ack.acknowledge() }
        verify(exactly = 1) { ack.nack(Duration.ZERO) }
        confirmVerified(kafkaTemplate, ack)
    }

    @Test
    fun `processGeneric should throw error when required fields are missing`() {
        val event = mockk<GenericRecord>()
        assertThrows<UnrecoverableParseException> { circuitBreaker.processGeneric(event) }
    }

    @Test
    fun `processGeneric should handle valid dataset reasoned event`() {
        val event = mockk<GenericRecord>()
        every { event.get("type") } returns DatasetEventType.DATASET_REASONED.name
        every { event.get("fdkId") } returns "fdk-id"
        every { event.get("graph") } returns "graph"
        every { event.get("timestamp") } returns 12345L
        every { datasetHandler.parseDataset(any(), any()) } returns mapper.readTree("{\"ok\":true}")
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()

        circuitBreaker.processGeneric(event)

        verify {
            kafkaTemplate.send(any(), match { it.fdkId == "fdk-id" && it.resourceType == RdfParseResourceType.DATASET })
        }
    }

    @Test
    fun `processDataset should handle valid dataset reasoned event`() {
        val event = mockk<DatasetEvent>()
        every { event.type } returns DatasetEventType.DATASET_REASONED
        every { event.fdkId } returns "fdk-id"
        every { event.graph } returns "graph"
        every { event.timestamp } returns 12345L
        every { datasetHandler.parseDataset(any(), any()) } returns mapper.readTree("{\"ok\":true}")
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()

        circuitBreaker.processDataset(event)

        verify {
            kafkaTemplate.send(any(), match { it.fdkId == "fdk-id" && it.resourceType == RdfParseResourceType.DATASET })
        }
    }

}
