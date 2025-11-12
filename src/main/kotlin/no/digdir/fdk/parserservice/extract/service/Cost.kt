package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.service.ServiceCost
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms

private fun ServiceCost.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        description != null -> true
        currency != null -> true
        ifAccessedThrough != null -> true
        value != null -> true
        else -> false
    }

private fun Resource.buildServiceCost(): ServiceCost? {
    val builder = ServiceCost.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setCurrency(extractStringValue(CV.currency))
        .setIfAccessedThrough(extractStringValue(CV.ifAccessedThrough))
        .setValue(extractStringValue(CV.value))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts all service cost resources and converts them to `ServiceCost`
 * objects containing cost metadata such as value, currency, description,
 * and the channel through which the cost applies.
 *
 * @return list of service costs or `null` when no cost information exists
 */
fun Resource.extractListOfServiceCosts(): List<ServiceCost>? =
    listResources(CV.hasCost)
        ?.mapNotNull { it.buildServiceCost() }
        ?.takeIf { it.isNotEmpty() }
