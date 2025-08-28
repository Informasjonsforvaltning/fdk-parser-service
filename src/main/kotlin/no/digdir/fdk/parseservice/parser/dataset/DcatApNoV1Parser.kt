package no.digdir.fdk.parseservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.descriptionHtmlCleaner
import no.digdir.fdk.parseservice.extract.extractListOfStrings
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.extractOrganization
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.namespace.ADMS
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms

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

        val builder = getDatasetBuilder(recordResource, datasetResource)

        val formattedDescription = datasetResource.extractLocalizedStrings(DCTerms.description)

        builder.setTitle(datasetResource.extractLocalizedStrings(DCTerms.title))
        builder.setDescriptionFormatted(formattedDescription)
        builder.setDescription(formattedDescription?.descriptionHtmlCleaner())
        builder.setPublisher(datasetResource.extractOrganization(DCTerms.publisher))
        builder.setIdentifier(datasetResource.extractListOfStrings(DCTerms.identifier))
        builder.setAdmsIdentifier(datasetResource.extractListOfStrings(ADMS.identifier))
        builder.setModified(datasetResource.extractStringValue(DCTerms.modified))
        builder.setIssued(datasetResource.extractStringValue(DCTerms.issued))
        builder.setLandingPage(datasetResource.extractListOfStrings(DCAT.landingPage))
        builder.setPage(datasetResource.extractListOfStrings(FOAF.page))

        builder.setDistribution(null)
        builder.setSample(null)
        builder.setThemeUris(null)
        builder.setTheme(null)
        builder.setLosTheme(null)
        builder.setEurovocThemes(null)
        builder.setKeyword(null)
        builder.setDctType(null)
        builder.setAccessRights(null)
        builder.setLanguage(null)
        builder.setTemporal(null)
        builder.setSubject(null)
        builder.setSpatial(null)
        builder.setProvenance(null)
        builder.setAccrualPeriodicity(null)
        builder.setLegalBasisForProcessing(null)
        builder.setLegalBasisForRestriction(null)
        builder.setLegalBasisForAccess(null)
        builder.setConformsTo(null)
        builder.setInformationModel(null)
        builder.setHasRelevanceAnnotation(null)
        builder.setHasCurrentnessAnnotation(null)
        builder.setHasCompletenessAnnotation(null)
        builder.setHasAvailabilityAnnotation(null)
        builder.setHasAccuracyAnnotation(null)
        builder.setQualifiedAttributions(null)
        builder.setInSeries(null)
        builder.setLast(null)
        builder.setPrev(null)
        builder.setDatasetsInSeries(null)
        builder.setType(null)
        builder.setSpecializedType(null)

        return builder.build()
    }
}
