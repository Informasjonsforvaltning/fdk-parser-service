package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.service.ServiceOutput
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.EUAT
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

private fun ServiceOutput.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        name != null -> true
        description != null -> true
        language != null -> true
        type != null -> true
        else -> false
    }

private fun Resource.buildServiceOutput(): ServiceOutput? {
    val builder = ServiceOutput.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setName(extractLocalizedStrings(DCTerms.title))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setLanguage(extractListOfReferenceDataCodes(DCTerms.language, EUAT.authorityCode, SKOS.prefLabel))
        .setType(extractListOfReferenceDataCodes(DCTerms.type, "#", SKOS.prefLabel))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts all service output resources and converts them to `ServiceOutput`
 * objects containing output metadata such as name, description, language, and type.
 *
 * @param predicate the property predicate that points to the output resource(s)
 * @return list of service outputs or `null` when no output information exists
 */
fun Resource.extractListOfServiceOutput(predicate: Property): List<ServiceOutput>? =
    listResources(predicate)
        ?.mapNotNull { it.buildServiceOutput() }
        ?.takeIf { it.isNotEmpty() }
