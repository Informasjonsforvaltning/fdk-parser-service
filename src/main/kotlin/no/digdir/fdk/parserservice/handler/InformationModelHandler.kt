package no.digdir.fdk.parserservice.handler

import no.digdir.fdk.model.informationmodel.InformationModel
import no.digdir.fdk.parserservice.extract.fdk.topicUriOfRecordWithID
import no.digdir.fdk.parserservice.model.NoAcceptableFDKRecordsException
import no.digdir.fdk.parserservice.parser.InformationModelParserRegistry
import no.digdir.fdk.parserservice.utils.avroToJson
import no.digdir.fdk.parserservice.utils.readTurtle
import org.apache.jena.rdf.model.ModelFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.JsonNode

@Service
class InformationModelHandler(
    private val parserRegistry: InformationModelParserRegistry,
) {
    /**
     * Parses an information model from RDF graph data and returns it as JSON.
     *
     * This method processes a Turtle-formatted RDF graph containing information model
     * information and converts it to a JSON representation using the registered
     * information model parsers. It attempts to parse the information model with all available
     * parsers in priority order and merges the results.
     *
     * @param fdkId The FDK identifier for the information model
     * @param graph The Turtle-formatted RDF graph containing the information model
     * @return JSON representation of the parsed information model
     * @throws NoAcceptableFDKRecordsException if no information model is found with the given identifier
     * @throws IllegalStateException if no parsers can successfully parse the information model
     */
    fun parseInformationModel(
        fdkId: String,
        graph: String,
    ): JsonNode {
        val model = ModelFactory.createDefaultModel()
        val informationModel: InformationModel =
            try {
                model.readTurtle(graph)
                val resourceIRI = topicUriOfRecordWithID(fdkId, model)
                if (resourceIRI != null) {
                    // Parse with all registered parsers in priority order
                    val parsedInformationModels = parserRegistry.parseWithAllParsers(model, resourceIRI, fdkId)

                    // Use the first successfully parsed information model
                    parsedInformationModels.first()
                } else {
                    throw NoAcceptableFDKRecordsException("No information model found with identifier '$fdkId'")
                }
            } finally {
                model.close()
            }

        return avroToJson(informationModel, informationModel.schema)
    }
}
