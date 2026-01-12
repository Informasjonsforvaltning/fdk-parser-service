package no.digdir.fdk.parserservice.extract.concept

import no.digdir.fdk.model.concept.ConceptSubject
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

private fun ConceptSubject.hasContent() =
    when {
        uri != null -> true
        label != null -> true
        else -> false
    }

private fun Resource.buildFromResource(): ConceptSubject? {
    val builder = ConceptSubject.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setLabel(extractLocalizedStrings(SKOS.prefLabel))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts labels from subjects linked to the concept
 *
 * @return list of concept subjects or `null` when none are declared
 */
fun Resource.extractListOfConceptSubjects(): List<ConceptSubject>? {
    val resourceSubjects =
        listResources(DCTerms.subject)
            ?.mapNotNull { it.buildFromResource() }

    val literalValue =
        extractLocalizedStrings(DCTerms.subject)?.let {
            ConceptSubject
                .newBuilder()
                .setUri(null)
                .setLabel(extractLocalizedStrings(SKOS.prefLabel))
                .build()
        }

    return when {
        !resourceSubjects.isNullOrEmpty() && literalValue != null -> resourceSubjects + listOf(literalValue)
        resourceSubjects.isNullOrEmpty() && literalValue != null -> listOf(literalValue)
        !resourceSubjects.isNullOrEmpty() && literalValue == null -> resourceSubjects
        else -> null
    }
}
