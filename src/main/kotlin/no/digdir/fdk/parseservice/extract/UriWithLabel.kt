package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.UriWithLabel
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement

fun Resource.extractListOfUriWithLabel(pred: Property, uriPred: Property, labelPred: Property): List<UriWithLabel>? =
    listProperties(pred)
        .asSequence()
        .mapNotNull { it.buildUriWithLabel(uriPred, labelPred) }
        .toList()
        .takeIf { it.isNotEmpty() }

private fun Statement.buildUriWithLabel(uriPred: Property, labelPred: Property): UriWithLabel? {
    val builder = UriWithLabel.newBuilder()
    val uriValueFromPredicate = resource.extractStringValue(uriPred)
    val uriValueFromResource = resource.extractURIStringValue()

    return builder
        .setUri(uriValueFromPredicate ?: uriValueFromResource)
        .setPrefLabel(resource.extractLocalizedStrings(labelPred))
        .build()
        .takeIf { it.uri != null || it.prefLabel != null }
}
