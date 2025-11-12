package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.service.ServiceLegalResource
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDFS

private fun ServiceLegalResource.hasContent() =
    when {
        uri != null -> true
        dctTitle != null -> true
        description != null -> true
        seeAlso != null -> true
        relation != null -> true
        else -> false
    }

private fun Resource.buildServiceLegalResource(): ServiceLegalResource? {
    val builder = ServiceLegalResource.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setDctTitle(extractLocalizedStrings(DCTerms.title))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setSeeAlso(extractListOfStrings(RDFS.seeAlso))
        .setRelation(extractListOfStrings(DCTerms.relation))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts all service legal resource resources and converts them to `ServiceLegalResource`
 * objects containing legal resource metadata such as title, description, seeAlso references,
 * and related legal resources.
 *
 * @param predicate the property predicate that points to the legal resource(s)
 * @return list of service legal resources or `null` when no legal resource information exists
 */
fun Resource.extractListOfServiceLegalResources(predicate: Property): List<ServiceLegalResource>? =
    listResources(predicate)
        ?.mapNotNull { it.buildServiceLegalResource() }
        ?.takeIf { it.isNotEmpty() }
