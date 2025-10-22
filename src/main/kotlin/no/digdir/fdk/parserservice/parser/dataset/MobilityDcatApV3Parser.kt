package no.digdir.fdk.parserservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.dataset.extractListOfQualifiedAttributions
import no.digdir.fdk.parserservice.extract.dataset.extractQualityAnnotation
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractListOfTemporal
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.DQVISO
import no.digdir.fdk.parserservice.vocabulary.MobilityDCAT
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS
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

        builder.setMobilityTheme(datasetResource.extractListOfReferenceDataCodes(MobilityDCAT.mobilityTheme, "/", SKOS.prefLabel))
        builder.setTemporal(datasetResource.extractListOfTemporal(DCTerms.temporal, DCAT.startDate, DCAT.endDate))

        builder.setDistribution(null)
        builder.setSample(null)

        builder.setHasRelevanceAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Relevance))
        builder.setHasCurrentnessAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Currentness))
        builder.setHasCompletenessAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Completeness))
        builder.setHasAvailabilityAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Availability))
        builder.setHasAccuracyAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Accuracy))
        builder.setQualifiedAttributions(datasetResource.extractListOfQualifiedAttributions())

        // The following properties are not implemented in mobilityDCAT-AP v3.0.0
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
