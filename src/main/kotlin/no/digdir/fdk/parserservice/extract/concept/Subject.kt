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

private fun Resource.buildFromLiteralValue(): ConceptSubject? {
    val literalValue = extractLocalizedStrings(DCTerms.subject)
    return if (literalValue != null) {
        ConceptSubject
            .newBuilder()
            .setUri(null)
            .setLabel(literalValue)
            .build()
    } else {
        null
    }
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
            ?: emptyList()

    val literalValue =
        buildFromLiteralValue()
            ?.let { listOf(it) }
            ?: emptyList()

    return if (resourceSubjects.isEmpty() && literalValue.isEmpty()) {
        null
    } else {
        resourceSubjects + literalValue
    }
}

/**
 * Extracts subjects for a concept using the V1 approach.
 *
 * Unlike the V2 approach which combines both resource-based and literal subjects, this method
 * only returns the literal value if present.
 *
 * @receiver The RDF resource representing the concept
 * @return A list containing a single `ConceptSubject` with the literal label, or `null` if not found
 * @see extractListOfConceptSubjects
 * @see ConceptSubject
 */
fun Resource.extractListOfConceptSubjectsV1(): List<ConceptSubject>? = buildFromLiteralValue()?.let { listOf(it) }
