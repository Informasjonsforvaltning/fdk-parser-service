package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.dataset.Subject
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.extract.listResources
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

private fun Subject.hasContent() = when {
    uri != null -> true
    identifier != null -> true
    prefLabel != null -> true
    definition != null -> true
    else -> false
}

private fun Resource.buildSubject(): Subject? {
    val builder = Subject.newBuilder()

    builder.setUri(if (isURIResource) uri else null)
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setPrefLabel(extractLocalizedStrings(SKOS.prefLabel))
        .setDefinition(extractLocalizedStrings(SKOS.definition))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.extractListOfSubjects(): List<Subject>? =
    listResources(DCTerms.subject)
        ?.mapNotNull { it.buildSubject() }
        ?.takeIf { it.isNotEmpty() }
