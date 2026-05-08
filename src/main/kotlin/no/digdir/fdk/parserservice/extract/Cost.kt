package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.Cost
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.SKOS

private fun Cost.hasContent() =
    when {
        identifier != null -> true
        hasValue != null -> true
        description != null -> true
        documentation != null -> true
        currency != null -> true
        isDefinedBy != null -> true
        else -> false
    }

private fun Resource.builderWithCommonProperties(): Cost.Builder =
    Cost
        .newBuilder()
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setHasValue(extractStringValue(CV.hasValue))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setDocumentation(extractListOfStrings(FOAF.page))
        .setCurrency(extractReferenceDataCode(CV.currency, DC_11.identifier, SKOS.prefLabel))

private fun Resource.buildCost(): Cost? {
    val builder = builderWithCommonProperties()

    builder.setIsDefinedBy(null)

    return builder.build().takeIf { it.hasContent() }
}

private fun Resource.buildServiceCost(): Cost? {
    val builder = builderWithCommonProperties()

    builder.setIsDefinedBy(extractListOfOrganizations(CV.isDefinedBy))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.extractListOfCosts(): List<Cost>? =
    listResources(CV.hasCost)
        ?.mapNotNull { it.buildCost() }
        ?.takeIf { it.isNotEmpty() }

fun Resource.extractListOfServiceCosts(): List<Cost>? =
    listResources(CV.hasCost)
        ?.mapNotNull { it.buildServiceCost() }
        ?.takeIf { it.isNotEmpty() }
