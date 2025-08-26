package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.descriptionHtmlCleaner
import no.digdir.fdk.parseservice.extract.extractListOfStrings
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.extractPublisher
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.namespace.ADMS
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms

/**
 * Extraction of dataset values based on v1.1 of dcat-ap-no, see https://data.norge.no/specification/dcat-ap-no/v1.1
 */
fun Dataset.addV11Values(datasetResource: Resource): Dataset {
    val formattedDescription = datasetResource.extractLocalizedStrings(DCTerms.description)

    title = datasetResource.extractLocalizedStrings(DCTerms.title)
    descriptionFormatted = formattedDescription
    description = formattedDescription?.descriptionHtmlCleaner()
    publisher = datasetResource.extractPublisher()
    identifier = datasetResource.extractListOfStrings(DCTerms.identifier)
    admsIdentifier = datasetResource.extractListOfStrings(ADMS.identifier)
    modified = datasetResource.extractStringValue(DCTerms.modified)
    issued = datasetResource.extractStringValue(DCTerms.issued)
    landingPage = datasetResource.extractListOfStrings(DCAT.landingPage)
    page = datasetResource.extractListOfStrings(FOAF.page)
    return this
}
