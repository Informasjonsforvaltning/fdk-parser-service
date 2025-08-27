package no.digdir.fdk.parseservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.dataset.extractListOfDistributionsV1
import no.digdir.fdk.parseservice.extract.fdk.addFdkData
import no.digdir.fdk.parseservice.extract.fdk.fdkRecord
import no.digdir.fdk.parseservice.extract.fdk.primaryTopicFromFdkRecord
import no.digdir.fdk.parseservice.namespace.ADMS
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT

/**
 * Parser for DCAT-AP-NO version 1.1
 * Based on https://data.norge.no/specification/dcat-ap-no/v1.1
 */
class DcatApNoV1Parser : BaseDatasetParser() {
    override fun getDefaultLanguage(): String = "no"

    override fun getVersion(): String = "1.1"

    override fun getSourceFormat(): String = "DCAT-AP-NO"

    override fun getFDKURIPattern(): String = "fellesdatakatalog.digdir.no"

    override fun getAcceptableTypes(): List<Resource> = listOf(DCAT.Dataset)

    override fun parse(model: Model): Dataset {
        val recordResource = fdkRecord(model, getAcceptableTypes(), getFDKURIPattern())
        val datasetResource = primaryTopicFromFdkRecord(recordResource, getAcceptableTypes())

        val builder = Dataset.newBuilder()

        builder.addFdkData(recordResource, datasetResource)

        builder.addCommonDatasetValues(datasetResource)

        builder.setDistribution(datasetResource.extractListOfDistributionsV1(DCAT.distribution))
        builder.setSample(datasetResource.extractListOfDistributionsV1(ADMS.sample))

        // The following properties are not implemented in DCAT-AP-NO v1.1
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
