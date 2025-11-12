package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.service.ServiceRule
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.CPSV
import no.digdir.fdk.parserservice.vocabulary.EUAT
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

private fun ServiceRule.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        name != null -> true
        description != null -> true
        language != null -> true
        legalResources != null -> true
        else -> false
    }

private fun Resource.buildServiceRule(): ServiceRule? {
    val builder = ServiceRule.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setName(extractLocalizedStrings(DCTerms.title))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setLanguage(extractListOfReferenceDataCodes(DCTerms.language, EUAT.authorityCode, SKOS.prefLabel))
        .setLegalResources(extractListOfStrings(CPSV.implements))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts all service rule resources and converts them to `ServiceRule`
 * objects containing rule metadata such as name, description, language,
 * and the legal resources that the rule implements.
 *
 * @return list of service rules or `null` when no rule information exists
 */
fun Resource.extractListOfServiceRules(): List<ServiceRule>? =
    listResources(CPSV.follows)
        ?.mapNotNull { it.buildServiceRule() }
        ?.takeIf { it.isNotEmpty() }
