package no.digdir.fdk.parseservice.extract.fdk

import no.digdir.fdk.model.HarvestMetaData
import no.digdir.fdk.parseservice.extract.containsTriple
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.extract.singleObjectStatement
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF


/**
 * Extracts a fdkRecord from a graph.
 * @param model The jena model of the graph
 * @param acceptableTypes A list of RDF types that is acceptable as primary topic for a fdkRecord
 * @param fdkURIPattern A pattern for fdkRecord URIs
 */
fun fdkRecord(model: Model, acceptableTypes: List<Resource>, fdkURIPattern: String): Resource {
    val records = model.listResourcesWithProperty(RDF.type, DCAT.CatalogRecord)
        .asSequence()
        .filter { isValidFDKRecord(it, acceptableTypes, fdkURIPattern) }
        .toList()

    return when {
        records.isEmpty() -> throw Exception("No acceptable FDK records found in graph")
        records.size > 1 -> throw Exception("Multiple FDK records found in graph")
        else -> records.first()
    }
}

/**
 * Extracts the fdkId from a fdkRecord.
 * @param recordResource The jena resource of the fdkRecord
 */
fun fdkIdFromRecord(recordResource: Resource) : String? {
    return recordResource.singleObjectStatement(DCTerms.identifier)!!.string
}

/**
 * Extracts the jena resource of a primary topic from a fdkRecord,
 * will throw exceptions if the record does not follow the pattern of FDK records.
 * @param recordResource The jena resource of the fdkRecord
 * @param acceptableTypes A list of RDF types that is acceptable as primary topic for a fdkRecord
 */
fun primaryTopicFromFdkRecord(recordResource: Resource, acceptableTypes: List<Resource>): Resource {
    val primaryTopic = recordResource.listProperties(FOAF.primaryTopic)
        .asSequence()
        .mapNotNull { it.resource }
        .singleOrNull()
        ?: throw Exception("No primary topic found on record")

    if (acceptableTypes.none { recordResource.model.containsTriple(primaryTopic.uri, RDF.type.uri, it.uri, true) }) {
        throw Exception("Primary topic is not one of the acceptable types: ${acceptableTypes.joinToString(", ") { it.localName }}")
    }

    return primaryTopic
}

/**
 * Simple boolean check that the method primaryTopicFromFdkRecord does not throw any exceptions with the supplied CatalogRecord,
 * used for filtering CatalogRecords found on the graph.
 * Most graphs will at least have one extra CatalogRecord, one that supplies metadata about the harvested Catalog/Collection,
 * but it's also possible that there are more.
 * @param recordResource The jena resource of the fdkRecord
 * @param acceptableTypes A list of RDF types that is acceptable as primary topic for a fdkRecord
 */
private fun hasAcceptablePrimaryTopic(recordResource: Resource, acceptableTypes: List<Resource>): Boolean = try {
    primaryTopicFromFdkRecord(recordResource, acceptableTypes)
    true
} catch (ex: Exception) {
    false
}

/**
 * Determines if a CatalogRecord follows the pattern of CatalogRecords added by the FDK harvest process.
 * @param recordResource The jena resource of the fdkRecord
 * @param acceptableTypes A list of RDF types that is acceptable as primary topic for a fdkRecord
 * @param fdkURIPattern A pattern for fdkRecord URIs
 */
private fun isValidFDKRecord(recordResource: Resource, acceptableTypes: List<Resource>, fdkURIPattern: String): Boolean =
    when {
        !recordResource.isURIResource -> false // All FDK records are URI resources.
        !recordResource.uri.contains(fdkURIPattern) -> false // Checks that the URI follows the FDK pattern.
        !hasAcceptablePrimaryTopic(recordResource, acceptableTypes) -> false // Checks that it's possible to extract the primary topic from the record.
        else -> true
    }

/**
 * Constructs the object HarvestMetaData based on metadata added to the graph by the FDK harvest process.
 * @param recordResource The jena resource of the fdkRecord
 */
fun harvestMetaData(recordResource: Resource): HarvestMetaData? {
    val builder = HarvestMetaData.newBuilder()

    val harvest = builder.setModified(recordResource.extractStringValue(DCTerms.modified))
        .setFirstHarvested(recordResource.extractStringValue(DCTerms.issued))
        .build()

    return if (harvest.modified != null || harvest.firstHarvested != null) harvest else null
}
