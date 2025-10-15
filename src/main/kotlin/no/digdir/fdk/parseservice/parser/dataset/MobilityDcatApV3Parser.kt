package no.digdir.fdk.parseservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.containsTriple
import no.digdir.fdk.parseservice.extract.fdk.addFdkData
import no.digdir.fdk.parseservice.extract.fdk.fdkRecord
import no.digdir.fdk.parseservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parseservice.model.LanguageCodes
import no.digdir.fdk.parseservice.model.NoAcceptableTypesException
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component
import java.net.URI


@Component
class MobilityDcatApV3Parser() : BaseDatasetParser() {
    override fun getDefaultLanguage(): String = LanguageCodes.NORWEGIAN.code
    override fun getVersion(): String = "3.0.0"
    override fun getSourceFormat(): String = "mobilityDCAT-AP"
    override fun getAcceptableTypes(): List<Resource> = listOf(DCAT.Dataset)

    override fun parse(model: Model, iri: String): Dataset =
        parseDataset(model, iri, null)

    override fun parse(model: Model, iri: String, fdkId: String): Dataset =
        parseDataset(model, iri, fdkId)

    private fun parseDataset(model: Model, iri: String, fdkId: String?): Dataset {
        if (getAcceptableTypes().none { model.containsTriple(iri, RDF.type.uri, URI.create(it.uri)) }) {
            throw NoAcceptableTypesException("No acceptable types found for $iri")
        }

        val datasetResource = resourceOfIRI(model, iri)

        val builder = Dataset.newBuilder()

        if (fdkId != null) {
            val recordResource = fdkRecord(datasetResource, fdkId)
            builder.addFdkData(recordResource, datasetResource)
        }

        builder.addCommonDatasetValues(datasetResource)

        builder.setTemporal(null)
        builder.setDistribution(null)
        builder.setSample(null)

        builder.setHasRelevanceAnnotation(null)
        builder.setHasCurrentnessAnnotation(null)
        builder.setHasCompletenessAnnotation(null)
        builder.setHasAvailabilityAnnotation(null)
        builder.setHasAccuracyAnnotation(null)
        builder.setQualifiedAttributions(null)
        builder.setInSeries(null)
        builder.setDatasetsInSeries(null)
        builder.setLast(null)
        builder.setPrev(null)
        builder.setLegalBasisForProcessing(null)
        builder.setLegalBasisForRestriction(null)
        builder.setLegalBasisForAccess(null)
        builder.setSpecializedType(null)

        return builder.build()
    }
}
