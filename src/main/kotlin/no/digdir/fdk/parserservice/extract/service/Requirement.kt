package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.service.ServiceRequirement
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractListOfUriWithLabel
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

private fun ServiceRequirement.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        dctTitle != null -> true
        dctType != null -> true
        description != null -> true
        fulfils != null -> true
        else -> false
    }

private fun Resource.buildServiceRequirement(): ServiceRequirement? {
    val builder = ServiceRequirement.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setDctTitle(extractLocalizedStrings(DCTerms.title))
        .setDctType(extractListOfUriWithLabel(DCTerms.type, DCTerms.source, SKOS.prefLabel))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setFulfils(extractListOfStrings(CV.fulfils))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts all service requirement resources and converts them to `ServiceRequirement`
 * objects containing requirement metadata such as title, description, type, and
 * the rules that the requirement fulfils.
 *
 * @return list of service requirements or `null` when no requirement information exists
 */
fun Resource.extractListOfServiceRequirements(): List<ServiceRequirement>? =
    listResources(CV.holdsRequirement)
        ?.mapNotNull { it.buildServiceRequirement() }
        ?.takeIf { it.isNotEmpty() }
