package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.UriWithLabel
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement

/**
 * Extracts URI-with-label structures where the URI is taken from the first of the supplied
 * predicates that holds a value, or from the resource itself, and the label from the supplied
 * predicate.
 *
 * @param pred predicate pointing to the intermediate resource
 * @param uriPreds predicates used to extract the URI value, tried in order
 * @param labelPred predicate used to extract the localized label
 * @return list of `UriWithLabel` objects or `null` when none exist
 */
fun Resource.extractListOfUriWithLabel(pred: Property, uriPreds: List<Property>, labelPred: Property): List<UriWithLabel>? =
    listProperties(pred)
        .asSequence()
        .mapNotNull { it.buildUriWithLabel(uriPreds, labelPred) }
        .toList()
        .takeIf { it.isNotEmpty() }

/**
 * Extracts URI-with-label structures where the URI is taken either from a nested predicate
 * or from the resource itself and the label from the supplied predicate.
 *
 * @param pred predicate pointing to the intermediate resource
 * @param uriPred predicate used to extract the URI value
 * @param labelPred predicate used to extract the localized label
 * @return list of `UriWithLabel` objects or `null` when none exist
 */
fun Resource.extractListOfUriWithLabel(pred: Property, uriPred: Property, labelPred: Property): List<UriWithLabel>? =
    extractListOfUriWithLabel(pred, listOf(uriPred), labelPred)

/**
 * Extracts URI-with-label entries where the URI is taken directly from the linked resource
 * and labels are obtained via the provided predicate.
 *
 * @param pred predicate pointing to the resource(s) to convert
 * @param labelPred predicate supplying the localized label
 * @return list of `UriWithLabel` entries or `null` when no data is available
 */
fun Resource.extractListOfUriWithLabel(pred: Property, labelPred: Property): List<UriWithLabel>? =
    extractListOfUriWithLabel(pred, emptyList(), labelPred)

private fun Statement.buildUriWithLabel(uriPreds: List<Property>, labelPred: Property): UriWithLabel? {
    if (isResource(this)) {
        val builder = UriWithLabel.newBuilder()
        val uriValueFromPredicates = uriPreds.firstNotNullOfOrNull { resource.extractStringValue(it) }
        val uriValueFromResource = resource.extractURIStringValue()

        return builder
            .setUri(uriValueFromPredicates ?: uriValueFromResource)
            .setPrefLabel(resource.extractLocalizedStrings(labelPred))
            .build()
            .takeIf { it.uri != null || it.prefLabel != null }
    } else {
        return null
    }
}
