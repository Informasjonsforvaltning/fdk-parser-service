package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.UriWithLabelAndType
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS

/**
 * Extracts URI-with-label entries and augments them with an extra RDF type when present,
 * allowing consumers to differentiate between SKOS concepts and other resources.
 *
 * @param pred predicate pointing to the target resource(s)
 * @param uriPred predicate used to read the URI value when not directly available
 * @param labelPred predicate providing a localized label
 * @return list of `UriWithLabelAndType` objects or `null` when no resources exist
 */
fun Resource.extractListOfUriWithLabelAndType(
    pred: Property,
    uriPred: Property,
    labelPred: Property,
): List<UriWithLabelAndType>? =
    listProperties(pred)
        .asSequence()
        .mapNotNull { it.buildUriWithLabelAndType(uriPred, labelPred) }
        .toList()
        .takeIf { it.isNotEmpty() }

private fun Resource.extractNonConceptType(): String? =
    listProperties(RDF.type)
        .asSequence()
        .filter { isResource(it) && it.resource != SKOS.Concept }
        .firstOrNull()
        ?.resource
        ?.uri

private fun Statement.buildUriWithLabelAndType(
    uriPred: Property,
    labelPred: Property,
): UriWithLabelAndType? {
    if (isResource(this)) {
        val builder = UriWithLabelAndType.newBuilder()
        val uriValueFromPredicate = resource.extractStringValue(uriPred)
        val uriValueFromResource = resource.extractURIStringValue()

        return builder
            .setUri(uriValueFromPredicate ?: uriValueFromResource)
            .setExtraType(resource.extractNonConceptType())
            .setPrefLabel(resource.extractLocalizedStrings(labelPred))
            .build()
            .takeIf { it.uri != null || it.prefLabel != null }
    } else {
        return null
    }
}
