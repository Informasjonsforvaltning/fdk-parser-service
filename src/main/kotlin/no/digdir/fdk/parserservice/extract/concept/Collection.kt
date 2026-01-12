package no.digdir.fdk.parserservice.extract.concept

import no.digdir.fdk.model.concept.ConceptCollection
import no.digdir.fdk.parserservice.LOGGER
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractOrganization
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS
import java.net.URI

private fun ConceptCollection.hasData(): Boolean =
    when {
        uri != null -> true
        id != null -> true
        label != null -> true
        description != null -> true
        publisher != null -> true
        else -> false
    }

/**
 * Extracts collection metadata for the concept.
 *
 * @return a populated `ConceptCollection` instance or `null` when no collection context is available
 */
fun Resource.extractConceptCollection(): ConceptCollection? {
    val collectionResource = getCollectionResource()

    if (collectionResource == null) {
        return null
    } else {
        val builder = ConceptCollection.newBuilder()

        val collection =
            builder
                .setUri(collectionResource.extractURIStringValue())
                .setId(collectionResource.extractStringValue(DCTerms.identifier))
                .setLabel(collectionResource.extractLocalizedStrings(DCTerms.title))
                .setDescription(collectionResource.extractLocalizedStrings(DCTerms.description))
                .setPublisher(collectionResource.extractOrganization(DCTerms.publisher))
                .build()

        return if (collection.hasData()) collection else null
    }
}

private fun Resource.getCollectionResource(): Resource? {
    val collections =
        model
            .listSubjectsWithProperty(RDF.type, SKOS.Collection)
            .asSequence()
            .filter { it.isURIResource }
            .filter { model.containsTriple(it.uri, SKOS.member.uri, URI.create(uri)) }
            .toList()

    return when {
        collections.isEmpty() -> null
        collections.size == 1 -> collections.first()
        else -> {
            LOGGER.warn("Expecting 1 collection with $uri as a member, found ${collections.size}. Selecting random as backup")
            collections.first()
        }
    }
}
