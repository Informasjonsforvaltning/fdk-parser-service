package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.LegalResource
import no.digdir.fdk.parserservice.vocabulary.EUAT
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.SKOS

private fun LegalResource.hasContent() =
    when {
        uri != null -> true
        title != null -> true
        dctTitle != null -> true
        description != null -> true
        language != null -> true
        type != null -> true
        seeAlso != null -> true
        relation != null -> true
        else -> false
    }

private fun Resource.buildLegalResource(): LegalResource? {
    val builder = LegalResource.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setTitle(extractLocalizedStrings(DCTerms.title))
        .setDctTitle(extractLocalizedStrings(DCTerms.title)) // Deprecated
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setLanguage(extractListOfReferenceDataCodes(DCTerms.language, EUAT.authorityCode, SKOS.prefLabel))
        .setType(extractReferenceDataCode(DCTerms.type, "#", SKOS.prefLabel))
        .setSeeAlso(extractListOfStrings(RDFS.seeAlso))
        .setRelation(extractListOfStrings(DCTerms.relation))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts all legal resource resources and converts them to `LegalResource` objects
 * containing legal resource metadata such as title, description, seeAlso references,
 * and related legal resources.
 *
 * @param predicate the property predicate that points to the legal resource(s)
 * @return list of legal resources or `null` when no legal resource information exists
 */
fun Resource.extractListOfLegalResources(predicate: Property): List<LegalResource>? =
    listResources(predicate)
        ?.mapNotNull { it.buildLegalResource() }
        ?.takeIf { it.isNotEmpty() }
