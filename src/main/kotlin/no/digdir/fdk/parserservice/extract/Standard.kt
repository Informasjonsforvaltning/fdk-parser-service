package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.Standard
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDFS

/**
 * Extracts the standards (`dct:conformsTo`) of the resource. The links are taken from
 * `rdfs:seeAlso`, then `dct:source`, and otherwise from the standard resource itself.
 *
 * @return list of `Standard` objects or `null` when none exist
 */
fun Resource.extractListOfStandards(): List<Standard>? = listProperties(DCTerms.conformsTo)
    .asSequence()
    .mapNotNull { it.buildStandard() }
    .toList()
    .takeIf { it.isNotEmpty() }

private fun Statement.buildStandard(): Standard? {
    if (!isResource(this)) return null

    val links =
        resource.extractListOfStrings(RDFS.seeAlso)
            ?: resource.extractListOfStrings(DCTerms.source)
            ?: listOfNotNull(resource.extractURIStringValue()).takeIf { it.isNotEmpty() }

    return Standard
        .newBuilder()
        .setUri(links?.firstOrNull())
        .setSeeAlso(links)
        .setPrefLabel(resource.extractLocalizedStrings(DCTerms.title))
        .build()
        .takeIf { it.seeAlso != null || it.prefLabel != null }
}
