package no.digdir.fdk.parserservice.extract.concept

import no.digdir.fdk.model.UriWithText
import no.digdir.fdk.model.concept.ConceptDefinition
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.EUVOC
import no.digdir.fdk.parserservice.vocabulary.SKOSNO
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.SKOS

private fun Resource.extractSources(): List<UriWithText>? =
    listResources(DCTerms.source)
        ?.toList()
        ?.map { source ->
            UriWithText
                .newBuilder()
                .setUri(source.extractURIStringValue())
                .setText(source.extractLocalizedStrings(RDFS.label))
                .build()
        }?.filter { it.uri != null && it.text != null }
        ?.takeIf { it.isNotEmpty() }

private fun Resource.buildXlDefinition(): ConceptDefinition? {
    val builder = ConceptDefinition.newBuilder()

    builder
        .setText(extractLocalizedStrings(RDF.value))
        .setTargetGroup(extractStringValue(DCTerms.audience))
        .setSourceRelationship(extractStringValue(SKOSNO.relationshipWithSource))
        .setSources(extractSources())

    return builder.build().takeIf { it.text != null }
}

private fun Resource.extractXlDefinitions(): List<ConceptDefinition>? =
    listResources(EUVOC.xlDefinition)
        ?.mapNotNull { it.buildXlDefinition() }
        ?.takeIf { it.isNotEmpty() }

/**
 * Extracts all definitions for a concept from the RDF resource.
 *
 * This function handles and combines two types of definitions:
 * 1. **Extended definitions** (`euvoc:xlDefinition`): Structured definitions that can include
 *    additional metadata such as target groups, source relationships, and reference sources.
 * 2. **Direct statement definitions** (`skos:definition`): Basic text definitions with localized strings.
 *
 * @receiver The RDF resource representing the concept
 * @return A list of `ConceptDefinition` objects if any definitions are found, or `null` if not.
 *
 * @see ConceptDefinition
 * @see EUVOC.xlDefinition
 * @see SKOS.definition
 */
fun Resource.extractDefinitions(): List<ConceptDefinition>? {
    val xlDefinitions = extractXlDefinitions()
    val directStatement =
        extractLocalizedStrings(SKOS.definition)
            ?.let { listOf(ConceptDefinition().apply { text = it }) }

    return when {
        !xlDefinitions.isNullOrEmpty() && directStatement != null -> xlDefinitions + directStatement
        !xlDefinitions.isNullOrEmpty() -> xlDefinitions
        directStatement != null -> directStatement
        else -> null
    }
}
