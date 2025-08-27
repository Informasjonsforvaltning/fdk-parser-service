package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.ReferenceDataCode
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource

private fun ReferenceDataCode.hasContent() = when {
    uri != null -> true
    code != null -> true
    prefLabel != null -> true
    else -> false
}

private fun Resource.buildReferenceDataCode(codePredicate: Property, labelPredicate: Property): ReferenceDataCode? {
    val builder = ReferenceDataCode.newBuilder()

    builder.setUri(if (isURIResource) uri else null)
        .setCode(extractStringValue(codePredicate))
        .setPrefLabel(extractLocalizedStrings(labelPredicate))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.extractReferenceDataCode(
    mainPredicate: Property,
    codePredicate: Property,
    labelPredicate: Property
): ReferenceDataCode? =
    singleResource(mainPredicate)
        ?.buildReferenceDataCode(codePredicate, labelPredicate)

fun Resource.extractListOfReferenceDataCodes(
    mainPredicate: Property,
    codePredicate: Property,
    labelPredicate: Property
): List<ReferenceDataCode>? =
    listResources(mainPredicate)
        ?.mapNotNull { it.buildReferenceDataCode(codePredicate, labelPredicate) }
        ?.takeIf { it.isNotEmpty() }
