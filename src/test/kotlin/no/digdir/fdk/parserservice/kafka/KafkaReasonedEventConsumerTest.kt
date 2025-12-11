package no.digdir.fdk.parserservice.kafka

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.digdir.fdk.parserservice.handler.DataServiceHandler
import no.digdir.fdk.parserservice.handler.DatasetHandler
import no.digdir.fdk.parserservice.handler.InformationModelHandler
import no.digdir.fdk.parserservice.model.RecoverableParseException
import no.digdir.fdk.parserservice.model.UnrecoverableParseException
import no.fdk.dataservice.DataServiceEvent
import no.fdk.dataservice.DataServiceEventType
import no.fdk.dataset.DatasetEvent
import no.fdk.dataset.DatasetEventType
import no.fdk.informationmodel.InformationModelEvent
import no.fdk.informationmodel.InformationModelEventType
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
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

@ActiveProfiles("test")
@Tag("unit")
class KafkaReasonedEventConsumerTest {
    private val datasetHandler: DatasetHandler = mockk()
    private val dataServiceHandler: DataServiceHandler = mockk()
    private val informationModelHandler: InformationModelHandler = mockk()
    private val kafkaTemplate: KafkaTemplate<String, RdfParseEvent> = mockk()
    private val harvestEventKafkaTemplate: KafkaTemplate<String, no.fdk.harvest.HarvestEvent> = mockk()
    private val ack: Acknowledgment = mockk()
    private val kafkaRdfParseEventProducer = KafkaRdfParseEventProducer(kafkaTemplate)
    private val kafkaHarvestEventProducer = KafkaHarvestEventProducer(harvestEventKafkaTemplate)
    private val circuitBreaker =
        KafkaReasonedEventCircuitBreaker(
            kafkaRdfParseEventProducer,
            kafkaHarvestEventProducer,
            dataServiceHandler,
            datasetHandler,
            informationModelHandler,
        )
    private val kafkaReasonedEventConsumer = KafkaReasonedEventConsumer(circuitBreaker)
    private val mapper = jacksonObjectMapper()

    @Test
    fun `dataset listener should produce a rdf parse event`() {
        val parsedJson = "{\"data\":\"my-parsed-rdf\"}"
        val timestamp = System.currentTimeMillis()
        every { datasetHandler.parseDataset(any(), any()) } returns mapper.readTree(parsedJson)
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent = DatasetEvent(DatasetEventType.DATASET_REASONED, "harvest-run-id", "uri", "my-id", "graph", timestamp)
        kafkaReasonedEventConsumer.datasetListener(
            record = ConsumerRecord("dataset-events", 0, 0, "my-id", datasetEvent as Object),
            ack = ack,
        )

        verify {
            kafkaTemplate.send(
                withArg {
                    assertEquals("rdf-parse-events", it)
                },
                withArg {
                    assertEquals("my-id", it.fdkId)
                    assertEquals(RdfParseResourceType.DATASET, it.resourceType)
                    assertEquals(parsedJson, it.data)
                    assertEquals(timestamp, it.timestamp)
                    assertEquals("harvest-run-id", it.harvestRunId)
                    assertEquals("uri", it.uri)
                },
            )
            harvestEventKafkaTemplate.send(any(), any())
            ack.acknowledge()
        }
        confirmVerified(kafkaTemplate, harvestEventKafkaTemplate, ack)
    }

    @Test
    fun `dataset listener should acknowledge when a recoverable exception occurs`() {
        every {
            datasetHandler.parseDataset(any(), any())
        } throws RecoverableParseException("Error parsing RDF: invalid rdf")
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent =
            DatasetEvent(DatasetEventType.DATASET_REASONED, "harvest-run-id", "uri", "my-id", "graph", System.currentTimeMillis())
        kafkaReasonedEventConsumer.datasetListener(
            record = ConsumerRecord("dataset-events", 0, 0, "my-id", datasetEvent as Object),
            ack = ack,
        )

        verify(exactly = 0) { kafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { harvestEventKafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { ack.acknowledge() }
        verify(exactly = 0) { ack.nack(Duration.ZERO) }
        confirmVerified(kafkaTemplate, harvestEventKafkaTemplate, ack)
    }

    @Test
    fun `dataset listener should not acknowledge when a unrecoverable exception occurs`() {
        every { datasetHandler.parseDataset(any(), any()) } throws UnrecoverableParseException("Error parsing RDF")
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.nack(Duration.ZERO) } returns Unit

        val datasetEvent =
            DatasetEvent(DatasetEventType.DATASET_REASONED, "harvest-run-id", "uri", "my-id", "graph", System.currentTimeMillis())
        kafkaReasonedEventConsumer.datasetListener(
            record = ConsumerRecord("dataset-events", 0, 0, "my-id", datasetEvent as Object),
            ack = ack,
        )

        verify(exactly = 0) { kafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { harvestEventKafkaTemplate.send(any(), any()) }
        verify(exactly = 0) { ack.acknowledge() }
        verify(exactly = 1) { ack.nack(Duration.ZERO) }
        confirmVerified(kafkaTemplate, harvestEventKafkaTemplate, ack)
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
        every { event.get("harvestRunId") } returns "harvest-run-id"
        every { event.get("uri") } returns "uri"
        every { datasetHandler.parseDataset(any(), any()) } returns mapper.readTree("{\"ok\":true}")
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()

        circuitBreaker.processGeneric(event)

        verify {
            kafkaTemplate.send(any(), match { it.fdkId == "fdk-id" && it.resourceType == RdfParseResourceType.DATASET })
            harvestEventKafkaTemplate.send(any(), any())
        }
    }

    @Test
    fun `processDataset should handle valid dataset reasoned event`() {
        val event = mockk<DatasetEvent>()
        every { event.type } returns DatasetEventType.DATASET_REASONED
        every { event.fdkId } returns "fdk-id"
        every { event.graph } returns "graph"
        every { event.timestamp } returns 12345L
        every { event.harvestRunId } returns "harvest-run-id"
        every { event.uri } returns "uri"
        every { datasetHandler.parseDataset(any(), any()) } returns mapper.readTree("{\"ok\":true}")
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()

        circuitBreaker.processDataset(event)

        verify {
            kafkaTemplate.send(any(), match { it.fdkId == "fdk-id" && it.resourceType == RdfParseResourceType.DATASET })
            harvestEventKafkaTemplate.send(any(), any())
        }
    }

    @Test
    fun `data service listener should produce a rdf parse event`() {
        val parsedJson = "{\"data\":\"my-parsed-rdf\"}"
        val timestamp = System.currentTimeMillis()
        every { dataServiceHandler.parseDataService(any(), any()) } returns mapper.readTree(parsedJson)
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val dataServiceEvent =
            DataServiceEvent(DataServiceEventType.DATA_SERVICE_REASONED, "harvest-run-id", "uri", "my-id", "graph", timestamp)
        kafkaReasonedEventConsumer.dataServiceListener(
            record = ConsumerRecord("data-service-events", 0, 0, "my-id", dataServiceEvent as Object),
            ack = ack,
        )

        verify {
            kafkaTemplate.send(
                withArg {
                    assertEquals("rdf-parse-events", it)
                },
                withArg {
                    assertEquals("my-id", it.fdkId)
                    assertEquals(RdfParseResourceType.DATA_SERVICE, it.resourceType)
                    assertEquals(parsedJson, it.data)
                    assertEquals(timestamp, it.timestamp)
                    assertEquals("harvest-run-id", it.harvestRunId)
                    assertEquals("uri", it.uri)
                },
            )
            harvestEventKafkaTemplate.send(any(), any())
            ack.acknowledge()
        }
        confirmVerified(kafkaTemplate, harvestEventKafkaTemplate, ack)
    }

    @Test
    fun `data service listener should acknowledge when a recoverable exception occurs`() {
        every { dataServiceHandler.parseDataService(any(), any()) } throws RecoverableParseException("Error parsing RDF: invalid rdf")
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val dataServiceEvent =
            DataServiceEvent(
                DataServiceEventType.DATA_SERVICE_REASONED,
                "harvest-run-id",
                "uri",
                "my-id",
                "graph",
                System.currentTimeMillis(),
            )
        kafkaReasonedEventConsumer.dataServiceListener(
            record = ConsumerRecord("data-service-events", 0, 0, "my-id", dataServiceEvent as Object),
            ack = ack,
        )

        verify(exactly = 0) { kafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { harvestEventKafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { ack.acknowledge() }
        verify(exactly = 0) { ack.nack(Duration.ZERO) }
        confirmVerified(kafkaTemplate, harvestEventKafkaTemplate, ack)
    }

    @Test
    fun `data service listener should not acknowledge when a unrecoverable exception occurs`() {
        every { dataServiceHandler.parseDataService(any(), any()) } throws UnrecoverableParseException("Error parsing RDF")
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.nack(Duration.ZERO) } returns Unit

        val dataServiceEvent =
            DataServiceEvent(
                DataServiceEventType.DATA_SERVICE_REASONED,
                "harvest-run-id",
                "uri",
                "my-id",
                "graph",
                System.currentTimeMillis(),
            )
        kafkaReasonedEventConsumer.dataServiceListener(
            record = ConsumerRecord("data-service-events", 0, 0, "my-id", dataServiceEvent as Object),
            ack = ack,
        )

        verify(exactly = 0) { kafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { harvestEventKafkaTemplate.send(any(), any()) }
        verify(exactly = 0) { ack.acknowledge() }
        verify(exactly = 1) { ack.nack(Duration.ZERO) }
        confirmVerified(kafkaTemplate, harvestEventKafkaTemplate, ack)
    }

    @Test
    fun `processDataService should handle valid data service reasoned event`() {
        val event = mockk<DataServiceEvent>()
        every { event.type } returns DataServiceEventType.DATA_SERVICE_REASONED
        every { event.fdkId } returns "fdk-id"
        every { event.graph } returns "graph"
        every { event.timestamp } returns 12345L
        every { event.harvestRunId } returns "harvest-run-id"
        every { event.uri } returns "uri"
        every { dataServiceHandler.parseDataService(any(), any()) } returns mapper.readTree("{\"ok\":true}")
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()

        circuitBreaker.processDataService(event)

        verify {
            kafkaTemplate.send(any(), match { it.fdkId == "fdk-id" && it.resourceType == RdfParseResourceType.DATA_SERVICE })
            harvestEventKafkaTemplate.send(any(), any())
        }
    }

    @Test
    fun `information model listener should produce a rdf parse event`() {
        val parsedJson = "{\"data\":\"my-parsed-rdf\"}"
        val timestamp = System.currentTimeMillis()
        every { informationModelHandler.parseInformationModel(any(), any()) } returns mapper.readTree(parsedJson)
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val informationModelEvent =
            InformationModelEvent(
                InformationModelEventType.INFORMATION_MODEL_REASONED,
                "harvest-run-id",
                "uri",
                "my-id",
                "graph",
                timestamp,
            )
        kafkaReasonedEventConsumer.informationModelListener(
            record = ConsumerRecord("information-model-events", 0, 0, "my-id", informationModelEvent as Object),
            ack = ack,
        )

        verify {
            kafkaTemplate.send(
                withArg {
                    assertEquals("rdf-parse-events", it)
                },
                withArg {
                    assertEquals("my-id", it.fdkId)
                    assertEquals(RdfParseResourceType.INFORMATION_MODEL, it.resourceType)
                    assertEquals(parsedJson, it.data)
                    assertEquals(timestamp, it.timestamp)
                    assertEquals("harvest-run-id", it.harvestRunId)
                    assertEquals("uri", it.uri)
                },
            )
            harvestEventKafkaTemplate.send(any(), any())
            ack.acknowledge()
        }
        confirmVerified(kafkaTemplate, harvestEventKafkaTemplate, ack)
    }

    @Test
    fun `information model listener should acknowledge when a recoverable exception occurs`() {
        every { informationModelHandler.parseInformationModel(any(), any()) } throws
            RecoverableParseException("Error parsing RDF: invalid rdf")
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.acknowledge() } returns Unit
        every { ack.nack(Duration.ZERO) } returns Unit

        val informationModelEvent =
            InformationModelEvent(
                InformationModelEventType.INFORMATION_MODEL_REASONED,
                "harvest-run-id",
                "uri",
                "my-id",
                "graph",
                System.currentTimeMillis(),
            )
        kafkaReasonedEventConsumer.informationModelListener(
            record = ConsumerRecord("information-model-events", 0, 0, "my-id", informationModelEvent as Object),
            ack = ack,
        )

        verify(exactly = 0) { kafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { harvestEventKafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { ack.acknowledge() }
        verify(exactly = 0) { ack.nack(Duration.ZERO) }
        confirmVerified(kafkaTemplate, harvestEventKafkaTemplate, ack)
    }

    @Test
    fun `information model listener should not acknowledge when a unrecoverable exception occurs`() {
        every { informationModelHandler.parseInformationModel(any(), any()) } throws UnrecoverableParseException("Error parsing RDF")
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { ack.nack(Duration.ZERO) } returns Unit

        val informationModelEvent =
            InformationModelEvent(
                InformationModelEventType.INFORMATION_MODEL_REASONED,
                "harvest-run-id",
                "uri",
                "my-id",
                "graph",
                System.currentTimeMillis(),
            )
        kafkaReasonedEventConsumer.informationModelListener(
            record = ConsumerRecord("information-model-events", 0, 0, "my-id", informationModelEvent as Object),
            ack = ack,
        )

        verify(exactly = 0) { kafkaTemplate.send(any(), any()) }
        verify(exactly = 1) { harvestEventKafkaTemplate.send(any(), any()) }
        verify(exactly = 0) { ack.acknowledge() }
        verify(exactly = 1) { ack.nack(Duration.ZERO) }
        confirmVerified(kafkaTemplate, harvestEventKafkaTemplate, ack)
    }

    @Test
    fun `processInformationModel should handle valid information model reasoned event`() {
        val event = mockk<InformationModelEvent>()
        every { event.type } returns InformationModelEventType.INFORMATION_MODEL_REASONED
        every { event.fdkId } returns "fdk-id"
        every { event.graph } returns "graph"
        every { event.timestamp } returns 12345L
        every { event.harvestRunId } returns "harvest-run-id"
        every { event.uri } returns "uri"
        every { informationModelHandler.parseInformationModel(any(), any()) } returns mapper.readTree("{\"ok\":true}")
        every { kafkaTemplate.send(any(), any()) } returns CompletableFuture()
        every { harvestEventKafkaTemplate.send(any(), any()) } returns CompletableFuture()

        circuitBreaker.processInformationModel(event)

        verify {
            kafkaTemplate.send(any(), match { it.fdkId == "fdk-id" && it.resourceType == RdfParseResourceType.INFORMATION_MODEL })
            harvestEventKafkaTemplate.send(any(), any())
        }
    }
}
