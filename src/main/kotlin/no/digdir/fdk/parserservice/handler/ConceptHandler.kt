package no.digdir.fdk.parserservice.handler

import no.digdir.fdk.model.concept.Concept
import no.digdir.fdk.parserservice.extract.fdk.topicUriOfRecordWithID
import no.digdir.fdk.parserservice.model.NoAcceptableFDKRecordsException
import no.digdir.fdk.parserservice.parser.ConceptParserRegistry
import no.digdir.fdk.parserservice.utils.avroToJson
import no.digdir.fdk.parserservice.utils.readTurtle
import org.apache.jena.rdf.model.ModelFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.JsonNode

/**
 * Handler for processing concept parsing requests.
 *
 * This service handles the parsing of RDF concept information according to
 * SKOS-AP-NO and converts it to JSON format using the registered concept parsers.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
class ConceptHandler(
    private val parserRegistry: ConceptParserRegistry,
) {
    /**
     * Parses a concept from RDF graph data and returns it as JSON.
     *
     * This method processes a Turtle-formatted RDF graph containing concept
     * information and converts it to a JSON representation using the registered
     * concept parsers. It attempts to parse the concept with all available
     * parsers in priority order and merges the results.
     *
     * @param fdkId The FDK identifier for the concept
     * @param graph The Turtle-formatted RDF graph containing the concept
     * @return JSON representation of the parsed concept
     * @throws NoAcceptableFDKRecordsException if no concept is found with the given identifier
     * @throws IllegalStateException if no parsers can successfully parse the concept
     */
    fun parseConcept(
        fdkId: String,
        graph: String,
    ): JsonNode {
        val model = ModelFactory.createDefaultModel()
        val concept: Concept =
            try {
                model.readTurtle(graph)
                val resourceIRI = topicUriOfRecordWithID(fdkId, model)
                if (resourceIRI != null) {
                    // Parse with all registered parsers in priority order
                    val parsedConcepts = parserRegistry.parseWithAllParsers(model, resourceIRI, fdkId)

                    // Use the first successfully parsed concept
                    parsedConcepts.first()
                } else {
                    throw NoAcceptableFDKRecordsException("No concept found with identifier '$fdkId'")
                }
            } finally {
                model.close()
            }

        return avroToJson(concept, concept.schema)
    }
}
