package no.digdir.fdk.parserservice.handler

import no.digdir.fdk.parserservice.extract.fdk.topicUriOfRecordWithID
import no.digdir.fdk.parserservice.model.NoAcceptableFDKRecordsException
import no.digdir.fdk.parserservice.parser.ServiceParserRegistry
import no.digdir.fdk.parserservice.utils.ServiceMerger
import no.digdir.fdk.parserservice.utils.avroToJson
import no.digdir.fdk.parserservice.utils.readTurtle
import org.apache.jena.rdf.model.ModelFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.JsonNode

/**
 * Handler for processing service parsing requests.
 *
 * This service handles the parsing of RDF service information and converts
 * it to JSON format using the registered service parsers.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
class ServiceHandler(
    private val parserRegistry: ServiceParserRegistry,
) {
    /**
     * Parses a service from RDF graph data and returns it as JSON.
     *
     * This method processes a Turtle-formatted RDF graph containing service
     * information and converts it to a JSON representation using the registered
     * service parsers. It attempts to parse the service with all available
     * parsers in priority order and uses the first successful result.
     *
     * @param fdkId The FDK identifier for the service
     * @param graph The Turtle-formatted RDF graph containing the service
     * @return JSON representation of the parsed service
     * @throws NoAcceptableFDKRecordsException if no service is found with the given identifier
     * @throws IllegalStateException if no parsers can successfully parse the service
     */
    fun parseService(
        fdkId: String,
        graph: String,
    ): JsonNode {
        val model = ModelFactory.createDefaultModel()
        val service: no.digdir.fdk.model.service.Service =
            try {
                model.readTurtle(graph)
                val resourceIRI = topicUriOfRecordWithID(fdkId, model)
                if (resourceIRI != null) {
                    // Parse with all registered parsers in priority order
                    val parsedServices = parserRegistry.parseWithAllParsers(model, resourceIRI, fdkId)

                    // Merge all successfully parsed services using the service merger
                    ServiceMerger.merge(parsedServices)
                } else {
                    throw NoAcceptableFDKRecordsException("No service found with identifier '$fdkId'")
                }
            } finally {
                model.close()
            }

        return avroToJson(service, service.schema)
    }
}
