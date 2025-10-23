package no.digdir.fdk.parserservice.handler

import no.digdir.fdk.model.dataservice.DataService
import no.digdir.fdk.parserservice.extract.fdk.topicUriOfRecordWithID
import no.digdir.fdk.parserservice.model.NoAcceptableFDKRecordsException
import no.digdir.fdk.parserservice.parser.DataServiceParserRegistry
import no.digdir.fdk.parserservice.utils.avroToJson
import no.digdir.fdk.parserservice.utils.readTurtle
import org.apache.jena.rdf.model.ModelFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.JsonNode

/**
 * Handler for processing data service parsing requests.
 *
 * This service handles the parsing of RDF data service information and converts
 * it to JSON format using the registered data service parsers.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
class DataServiceHandler(
    private val parserRegistry: DataServiceParserRegistry
) {

    /**
     * Parses a data service from RDF graph data and returns it as JSON.
     *
     * This method processes a Turtle-formatted RDF graph containing data service
     * information and converts it to a JSON representation using the registered
     * data service parsers. It attempts to parse the data service with all available
     * parsers in priority order and merges the results.
     *
     * @param fdkId The FDK identifier for the data service
     * @param graph The Turtle-formatted RDF graph containing the data service
     * @return JSON representation of the parsed data service
     * @throws NoAcceptableFDKRecordsException if no data service is found with the given identifier
     * @throws IllegalStateException if no parsers can successfully parse the data service
     */
    fun parseDataService(fdkId: String, graph: String): JsonNode {
        val model = ModelFactory.createDefaultModel()
        val dataService: DataService = try {
            model.readTurtle(graph)
            val resourceIRI = topicUriOfRecordWithID(fdkId, model)
            if (resourceIRI != null) {
                // Parse with all registered parsers in priority order
                val parsedDataServices = parserRegistry.parseWithAllParsers(model, resourceIRI, fdkId)
                
                // Use the first successfully parsed data service (highest priority)
                parsedDataServices.first()
            } else {
                throw NoAcceptableFDKRecordsException("No data service found with identifier '$fdkId'")
            }
        } finally {
            model.close()
        }

        return avroToJson(dataService, dataService.schema)
    }
}
