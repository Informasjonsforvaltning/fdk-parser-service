package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.service.ServiceCost
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractReferenceDataCode
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.SKOS

private fun ServiceCost.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        description != null -> true
        currency != null -> true
        currencyCode != null -> true
        ifAccessedThrough != null -> true
        value != null -> true
        else -> false
    }

private fun Resource.builderWithCommonValues(): ServiceCost.Builder {
    val builder = ServiceCost.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setIfAccessedThrough(extractStringValue(CV.ifAccessedThrough))

    return builder
}

private fun Resource.buildServiceCostV0(): ServiceCost? {
    val builder = builderWithCommonValues()

    builder
        .setCurrency(extractStringValue(CV.currency))
        .setCurrencyCode(null)
        .setValue(extractStringValue(CV.value))

    return builder.build().takeIf { it.hasContent() }
}

private fun Resource.buildServiceCostV1(): ServiceCost? {
    val builder = builderWithCommonValues()

    builder
        .setCurrency(null)
        .setCurrencyCode(extractReferenceDataCode(CV.currency, DC_11.identifier, SKOS.prefLabel))
        .setValue(extractStringValue(CV.hasValue))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts all V0 service cost resources and converts them to `ServiceCost`
 * objects containing cost metadata such as value, currency, description,
 * and the channel through which the cost applies.
 *
 * @return list of service costs or `null` when no cost information exists
 */
fun Resource.extractListOfServiceCostsV0(): List<ServiceCost>? =
    listResources(CV.hasCost)
        ?.mapNotNull { it.buildServiceCostV0() }
        ?.takeIf { it.isNotEmpty() }

/**
 * Extracts all V1 service cost resources and converts them to `ServiceCost`
 * objects containing cost metadata such as value, currency, description,
 * and the channel through which the cost applies.
 *
 * @return list of service costs or `null` when no cost information exists
 */
fun Resource.extractListOfServiceCostsV1(): List<ServiceCost>? =
    listResources(CV.hasCost)
        ?.mapNotNull { it.buildServiceCostV1() }
        ?.takeIf { it.isNotEmpty() }
