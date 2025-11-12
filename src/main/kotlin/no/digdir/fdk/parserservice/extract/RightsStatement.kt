package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.RightsStatement
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

/**
 * Extracts the rights statement associated with the resource, mapping both the type
 * (reference data) and localized description when present.
 *
 * @return populated `RightsStatement` or `null` when neither type nor description exists
 */
fun Resource.extractRightsStatement(): RightsStatement? =
    singleResource(DCTerms.rights)
        ?.buildRightsStatement()

private fun Resource.buildRightsStatement(): RightsStatement? =
    RightsStatement
        .newBuilder()
        .setType(extractReferenceDataCode(DCTerms.type, "/", SKOS.prefLabel))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .build()
        .takeIf { it.type != null || it.description != null }
