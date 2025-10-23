package no.digdir.fdk.parserservice.handler

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parserservice.extract.fdk.topicUriOfRecordWithID
import no.digdir.fdk.parserservice.model.NoAcceptableFDKRecordsException
import no.digdir.fdk.parserservice.parser.DatasetParserRegistry
import no.digdir.fdk.parserservice.utils.avroToJson
import no.digdir.fdk.parserservice.utils.DatasetMerger
import no.digdir.fdk.parserservice.utils.readTurtle
import org.apache.jena.rdf.model.ModelFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.JsonNode

@Service
class DatasetHandler(
    private val parserRegistry: DatasetParserRegistry
) {

    /**
     * Parses a dataset from RDF graph data and returns it as JSON.
     *
     * This method processes a Turtle-formatted RDF graph containing dataset
     * information and converts it to a JSON representation using the registered
     * dataset parsers. It attempts to parse the dataset with all available
     * parsers in priority order and merges the results.
     *
     * @param fdkId The FDK identifier for the dataset
     * @param graph The Turtle-formatted RDF graph containing the dataset
     * @return JSON representation of the parsed dataset
     * @throws NoAcceptableFDKRecordsException if no dataset is found with the given identifier
     * @throws IllegalStateException if no parsers can successfully parse the dataset
     */
    fun parseDataset(fdkId: String, graph: String): JsonNode {
        val model = ModelFactory.createDefaultModel()
        val dataset: Dataset = try {
            model.readTurtle(graph)
            val resourceIRI = topicUriOfRecordWithID(fdkId, model)
            if (resourceIRI != null) {
                // Parse with all registered parsers in priority order
                val parsedDatasets = parserRegistry.parseWithAllParsers(model, resourceIRI, fdkId)
                
                // Merge all successfully parsed datasets using the dataset merger
                DatasetMerger.merge(parsedDatasets)
            } else {
                throw NoAcceptableFDKRecordsException("No dataset found with identifier '$fdkId'")
            }
        } finally {
            model.close()
        }

        return avroToJson(dataset, dataset.schema)
    }

}
