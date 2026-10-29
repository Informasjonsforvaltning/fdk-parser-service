package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.RightsStatement
import no.digdir.fdk.parserservice.vocabulary.ODRS
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

/**
 * Extracts the rights statement associated with the resource, mapping the type
 * (reference data), localized description and guidelines for re-users when present.
 *
 * @return populated `RightsStatement` or `null` when none of them exists
 */
fun Resource.extractRightsStatement(): RightsStatement? = singleResource(DCTerms.rights)
    ?.buildRightsStatement()

private fun Resource.buildRightsStatement(): RightsStatement? = RightsStatement
    .newBuilder()
    .setType(extractReferenceDataCode(DCTerms.type, "/", SKOS.prefLabel))
    .setDescription(extractLocalizedStrings(DCTerms.description))
    .setReuserGuidelines(extractListOfStrings(ODRS.reuserGuidelines))
    .build()
    .takeIf { it.type != null || it.description != null || it.reuserGuidelines != null }
