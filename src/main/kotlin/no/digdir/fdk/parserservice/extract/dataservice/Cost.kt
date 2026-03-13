package no.digdir.fdk.parserservice.extract.dataservice

import no.digdir.fdk.model.dataservice.DataServiceCost
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractReferenceDataCode
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.SKOS

private fun DataServiceCost.hasContent() =
    when {
        hasValue != null -> true
        description != null -> true
        documentation != null -> true
        currency != null -> true
        else -> false
    }

private fun Resource.buildDataServiceCost(): DataServiceCost? {
    val builder = DataServiceCost.newBuilder()

    builder
        .setHasValue(extractStringValue(CV.hasValue))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setDocumentation(extractListOfStrings(FOAF.page))
        .setCurrency(extractReferenceDataCode(CV.currency, DC_11.identifier, SKOS.prefLabel))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.extractListOfDataServiceCosts(): List<DataServiceCost>? =
    listResources(CV.hasCost)
        ?.mapNotNull { it.buildDataServiceCost() }
        ?.takeIf { it.isNotEmpty() }
