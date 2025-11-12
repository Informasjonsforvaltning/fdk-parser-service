package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.UriWithLabel
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement

/**
 * Extracts URI-with-label structures where the URI is taken either from a nested predicate
 * or from the resource itself and the label from the supplied predicate.
 *
 * @param pred predicate pointing to the intermediate resource
 * @param uriPred predicate used to extract the URI value
 * @param labelPred predicate used to extract the localized label
 * @return list of `UriWithLabel` objects or `null` when none exist
 */
fun Resource.extractListOfUriWithLabel(
    pred: Property,
    uriPred: Property,
    labelPred: Property,
): List<UriWithLabel>? =
    listProperties(pred)
        .asSequence()
        .mapNotNull { it.buildUriWithLabel(uriPred, labelPred) }
        .toList()
        .takeIf { it.isNotEmpty() }

private fun Statement.buildUriWithLabel(
    uriPred: Property,
    labelPred: Property,
): UriWithLabel? {
    if (isResource(this)) {
        val builder = UriWithLabel.newBuilder()
        val uriValueFromPredicate = resource.extractStringValue(uriPred)
        val uriValueFromResource = resource.extractURIStringValue()

        return builder
            .setUri(uriValueFromPredicate ?: uriValueFromResource)
            .setPrefLabel(resource.extractLocalizedStrings(labelPred))
            .build()
            .takeIf { it.uri != null || it.prefLabel != null }
    } else {
        return null
    }
}

/**
 * Extracts URI-with-label entries where the URI is taken directly from the linked resource
 * and labels are obtained via the provided predicate.
 *
 * @param pred predicate pointing to the resource(s) to convert
 * @param labelPred predicate supplying the localized label
 * @return list of `UriWithLabel` entries or `null` when no data is available
 */
fun Resource.extractListOfUriWithLabel(
    pred: Property,
    labelPred: Property,
): List<UriWithLabel>? =
    listProperties(pred)
        .asSequence()
        .mapNotNull { it.buildUriWithLabel(labelPred) }
        .toList()
        .takeIf { it.isNotEmpty() }

private fun Statement.buildUriWithLabel(labelPred: Property): UriWithLabel? {
    if (isResource(this)) {
        val builder = UriWithLabel.newBuilder()

        return builder
            .setUri(resource.extractURIStringValue())
            .setPrefLabel(resource.extractLocalizedStrings(labelPred))
            .build()
            .takeIf { it.uri != null || it.prefLabel != null }
    } else {
        return null
    }
}
