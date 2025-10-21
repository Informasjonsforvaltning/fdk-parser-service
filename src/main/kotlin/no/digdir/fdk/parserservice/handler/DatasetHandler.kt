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
