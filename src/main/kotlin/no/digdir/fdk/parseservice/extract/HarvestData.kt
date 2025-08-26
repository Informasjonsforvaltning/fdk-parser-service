package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.HarvestMetaData
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF

fun Model.fdkRecord(acceptableTypes: List<Resource>): Resource {
    val records = listResourcesWithProperty(RDF.type, DCAT.CatalogRecord)
        .asSequence()
        .filter { it.uri.contains("fellesdatakatalog.digdir.no") }
        .filter { it.hasAcceptablePrimaryTopic(acceptableTypes) }.toList()

    return when {
        records.isEmpty() -> throw Exception("No acceptable FDK records found in graph")
        records.size > 1 -> throw Exception("Multiple FDK records found in graph")
        else -> records.first()
    }
}

private fun Resource.primaryTopic(): Resource? {
    return listProperties(FOAF.primaryTopic)
        .asSequence()
        .mapNotNull { it.resource }
        .singleOrNull()
}

fun Resource.primaryTopicFromFdkRecord(acceptableTypes: List<Resource>): Resource {
    val primaryTopic = primaryTopic()
        ?: throw Exception("No primary topic found on record")

    if (acceptableTypes.none { model.containsTriple(primaryTopic.uri, RDF.type.uri, it.uri, true) }) {
        throw Exception("Primary topic is not one of the acceptable types: ${acceptableTypes.joinToString(", ") { it.localName }}")
    }

    return primaryTopic
}

private fun Resource.hasAcceptablePrimaryTopic(acceptableTypes: List<Resource>): Boolean = try {
    primaryTopicFromFdkRecord(acceptableTypes)
    true
} catch (ex: Exception) {
    false
}

fun Resource.extractHarvestMetaData(): HarvestMetaData? {
    val builder = HarvestMetaData.newBuilder()

    val harvest = builder.setModified(extractStringValue(DCTerms.modified))
        .setFirstHarvested(extractStringValue(DCTerms.issued))
        .build()

    return if (harvest.modified != null || harvest.firstHarvested != null) harvest else null
}
