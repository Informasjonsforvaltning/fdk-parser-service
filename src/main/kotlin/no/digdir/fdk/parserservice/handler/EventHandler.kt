package no.digdir.fdk.parserservice.handler

import no.digdir.fdk.parserservice.extract.fdk.topicUriOfRecordWithID
import no.digdir.fdk.parserservice.model.NoAcceptableFDKRecordsException
import no.digdir.fdk.parserservice.parser.EventParserRegistry
import no.digdir.fdk.parserservice.utils.EventMerger
import no.digdir.fdk.parserservice.utils.avroToJson
import no.digdir.fdk.parserservice.utils.readTurtle
import org.apache.jena.rdf.model.ModelFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.JsonNode

/**
 * Handler for processing event parsing requests.
 *
 * This service handles the parsing of RDF event information and converts
 * it to JSON format using the registered event parsers.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
class EventHandler(
    private val parserRegistry: EventParserRegistry,
) {
    /**
     * Parses an event from RDF graph data and returns it as JSON.
     *
     * This method processes a Turtle-formatted RDF graph containing event
     * information and converts it to a JSON representation using the registered
     * event parsers. It attempts to parse the event with all available
     * parsers in priority order and merges the results.
     *
     * @param fdkId The FDK identifier for the event
     * @param graph The Turtle-formatted RDF graph containing the event
     * @return JSON representation of the parsed event
     * @throws NoAcceptableFDKRecordsException if no event is found with the given identifier
     * @throws IllegalStateException if no parsers can successfully parse the event
     */
    fun parseEvent(
        fdkId: String,
        graph: String,
    ): JsonNode {
        val model = ModelFactory.createDefaultModel()
        val event: no.digdir.fdk.model.event.Event =
            try {
                model.readTurtle(graph)
                val resourceIRI = topicUriOfRecordWithID(fdkId, model)
                if (resourceIRI != null) {
                    // Parse with all registered parsers in priority order
                    val parsedEvents = parserRegistry.parseWithAllParsers(model, resourceIRI, fdkId)

                    // Merge all successfully parsed events using the event merger
                    EventMerger.merge(parsedEvents)
                } else {
                    throw NoAcceptableFDKRecordsException("No event found with identifier '$fdkId'")
                }
            } finally {
                model.close()
            }

        return avroToJson(event, event.schema)
    }
}
