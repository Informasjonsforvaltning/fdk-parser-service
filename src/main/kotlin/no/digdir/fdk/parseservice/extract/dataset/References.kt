package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.UriWithLabel
import no.digdir.fdk.model.dataset.Reference
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.listResources
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDFS

private fun Reference.hasContent() = when {
    referenceType != null -> true
    source?.uri != null -> true
    source?.prefLabel != null -> true
    else -> false
}

private fun Resource.buildReference(predicate: Property): Reference? {
    val codeBuilder = ReferenceDataCode.newBuilder()
    codeBuilder.setUri(predicate.uri)
        .setCode(null)
        .setPrefLabel(null)

    val sourceBuilder = UriWithLabel.newBuilder()
    sourceBuilder.setUri(uri)
        .setPrefLabel(extractLocalizedStrings(RDFS.label))

    val referenceBuilder = Reference.newBuilder()

    referenceBuilder.setReferenceType(codeBuilder.build())
        .setSource(sourceBuilder.build())

    return referenceBuilder.build().takeIf { it.hasContent() }
}

fun Resource.extractListOfReferences(): List<Reference>? {
    val referencePredicates = listOf(
        DCTerms.hasVersion,
        DCTerms.isVersionOf,
        DCTerms.isPartOf,
        DCTerms.hasPart,
        DCTerms.references,
        DCTerms.isReferencedBy,
        DCTerms.replaces,
        DCTerms.isReplacedBy,
        DCTerms.requires,
        DCTerms.isRequiredBy,
        DCTerms.relation,
        DCTerms.source
    )

    return referencePredicates
        .mapNotNull { pred -> listResources(pred)?.mapNotNull { it.buildReference(pred) } }
        .flatten()
        .takeIf { it.isNotEmpty() }
}
