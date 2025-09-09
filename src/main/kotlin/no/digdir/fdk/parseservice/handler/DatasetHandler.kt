package no.digdir.fdk.parseservice.handler

import com.fasterxml.jackson.databind.JsonNode
import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.fdk.topicUriOfRecordWithID
import no.digdir.fdk.parseservice.model.NoAcceptableFDKRecordsException
import no.digdir.fdk.parseservice.parser.dataset.DcatApNoV1Parser
import no.digdir.fdk.parseservice.utils.avroToJson
import no.digdir.fdk.parseservice.utils.readTurtle
import org.apache.jena.rdf.model.ModelFactory
import org.springframework.stereotype.Service

@Service
class DatasetHandler(private val v1Parser: DcatApNoV1Parser) {

    fun parseDataset(fdkId: String, graph: String): JsonNode {
        val model = ModelFactory.createDefaultModel()
        val dataset: Dataset = try {
            model.readTurtle(graph)
            val resourceIRI = topicUriOfRecordWithID(fdkId, model)
            if (resourceIRI != null) {
                v1Parser.parse(model, resourceIRI, fdkId)
            } else {
                throw NoAcceptableFDKRecordsException("No dataset found with identifier '$fdkId'")
            }
        } finally {
            model.close()
        }

        return avroToJson(dataset, dataset.schema)
    }

}
