package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.RightsStatement
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

fun Resource.extractRightsStatement(): RightsStatement? =
    singleResource(DCTerms.rights)
        ?.buildRightsStatement()

private fun Resource.buildRightsStatement(): RightsStatement? =
    RightsStatement.newBuilder()
        .setType(extractReferenceDataCode(DCTerms.type, "/", SKOS.prefLabel))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .build()
        .takeIf { it.type != null || it.description != null }
