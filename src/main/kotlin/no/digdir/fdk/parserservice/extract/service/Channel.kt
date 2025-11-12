package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.service.ServiceChannel
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractReferenceDataCode
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.CPSV
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS
import org.apache.jena.vocabulary.VCARD4

private fun ServiceChannel.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        channelType != null -> true
        description != null -> true
        processingTime != null -> true
        hasInput != null -> true
        email != null -> true
        url != null -> true
        telephone != null -> true
        else -> false
    }

private fun Resource.buildServiceChannel(): ServiceChannel? {
    val builder = ServiceChannel.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setChannelType(extractReferenceDataCode(DCTerms.type, "#", SKOS.prefLabel))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setProcessingTime(extractStringValue(CV.processingTime))
        .setHasInput(extractListOfStrings(CPSV.hasInput))
        .setEmail(extractListOfStrings(VCARD4.hasEmail))
        .setUrl(extractListOfStrings(VCARD4.hasURL))
        .setTelephone(extractListOfStrings(VCARD4.hasTelephone))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts all service channel resources and converts them to `ServiceChannel`
 * objects containing channel metadata such as type, description, processing time,
 * contact information, and input requirements.
 *
 * @return list of service channels or `null` when no channel information exists
 */
fun Resource.extractListOfServiceChannels(): List<ServiceChannel>? =
    listResources(CV.hasChannel)
        ?.mapNotNull { it.buildServiceChannel() }
        ?.takeIf { it.isNotEmpty() }
