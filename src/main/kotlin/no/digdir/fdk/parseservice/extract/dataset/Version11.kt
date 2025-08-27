package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.descriptionHtmlCleaner
import no.digdir.fdk.parseservice.extract.extractListOfStrings
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.extractOrganization
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.namespace.ADMS
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms

/**
 * Extraction of dataset values based on v1.1 of dcat-ap-no, see https://data.norge.no/specification/dcat-ap-no/v1.1
 */
fun Resource.addV11ValuesToBuilder(builder: Dataset.Builder) {
    val formattedDescription = extractLocalizedStrings(DCTerms.description)

    builder.setTitle(extractLocalizedStrings(DCTerms.title))
    builder.setDescriptionFormatted(formattedDescription)
    builder.setDescription(formattedDescription?.descriptionHtmlCleaner())
    builder.setPublisher(extractOrganization(DCTerms.publisher))
    builder.setIdentifier(extractListOfStrings(DCTerms.identifier))
    builder.setAdmsIdentifier(extractListOfStrings(ADMS.identifier))
    builder.setModified(extractStringValue(DCTerms.modified))
    builder.setIssued(extractStringValue(DCTerms.issued))
    builder.setLandingPage(extractListOfStrings(DCAT.landingPage))
    builder.setPage(extractListOfStrings(FOAF.page))

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
}
