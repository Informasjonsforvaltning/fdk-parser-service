package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.UriWithLabelAndType
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS

fun Resource.extractListOfUriWithLabelAndType(pred: Property, uriPred: Property, labelPred: Property): List<UriWithLabelAndType>? =
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

private fun Statement.buildUriWithLabelAndType(uriPred: Property, labelPred: Property): UriWithLabelAndType? {
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
    } else return null
}
