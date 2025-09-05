package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.dataset.AccessService
import no.digdir.fdk.parseservice.extract.extractListOfUriWithLabelAndType
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.listResources
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

private fun AccessService.hasContent() = when {
    uri != null -> true
    title != null -> true
    description != null -> true
    endpointDescription != null -> true
    else -> false
}

private fun Resource.buildAccessService(): AccessService? {
    val builder = AccessService.newBuilder()

    builder.setUri(if (isURIResource) uri else null)
        .setTitle(extractLocalizedStrings(DCTerms.title))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setEndpointDescription(extractListOfUriWithLabelAndType(DCAT.endpointDescription, DCTerms.source, SKOS.prefLabel))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.extractListOfAccessServices(): List<AccessService>? =
    listResources(DCAT.accessService)
        ?.mapNotNull { it.buildAccessService() }
        ?.takeIf { it.isNotEmpty() }
