package no.digdir.fdk.parseservice.extract.fdk

import no.digdir.fdk.model.HarvestMetaData
import no.digdir.fdk.parseservice.extract.containsTriple
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.extract.isURIResource
import no.digdir.fdk.parseservice.extract.singleObjectStatement
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF


/**
 * Extracts a valid FDK record from an RDF model.
 * 
 * This function searches for DCAT CatalogRecord resources in the model and filters
 * them to find valid FDK records based on URI pattern and acceptable types.
 * 
 * @param model The Jena RDF model containing the graph
 * @param acceptableTypes A list of RDF types that are acceptable as primary topic for an FDK record
 * @param fdkURIPattern A pattern that FDK record URIs must contain
 * @return The valid FDK record resource
 * @throws Exception if no acceptable FDK records are found
 * @throws Exception if multiple FDK records are found
 * @see Model.listResourcesWithProperty
 * @see isValidFDKRecord
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
 * Extracts the FDK identifier from an FDK record.
 * 
 * @param recordResource The Jena resource representing the FDK record
 * @return The FDK identifier string, or null if not found
 * @see Resource.singleObjectStatement
 */
fun fdkIdFromRecord(recordResource: Resource) : String? {
    return recordResource.singleObjectStatement(DCTerms.identifier)!!.string
}

/**
 * Extracts the primary topic resource from an FDK record.
 * 
 * This function extracts the primary topic (usually a dataset) from an FDK record
 * and validates that it is one of the acceptable types.
 * 
 * @param recordResource The Jena resource representing the FDK record
 * @param acceptableTypes A list of RDF types that are acceptable as primary topic
 * @return The primary topic resource
 * @throws Exception if no primary topic is found
 * @throws Exception if the primary topic is not one of the acceptable types
 * @see Resource.listProperties
 * @see Model.containsTriple
 */
fun primaryTopicFromFdkRecord(recordResource: Resource, acceptableTypes: List<Resource>): Resource {
    val primaryTopic = recordResource.listProperties(FOAF.primaryTopic)
        .asSequence()
        .filter { isURIResource(it) }
        .map { it.resource }
        .singleOrNull()
        ?: throw Exception("No primary topic found on record")

    if (acceptableTypes.none { recordResource.model.containsTriple(primaryTopic.uri, RDF.type.uri, it.uri, true) }) {
        throw Exception("Primary topic is not one of the acceptable types: ${acceptableTypes.joinToString(", ") { it.localName }}")
    }

    return primaryTopic
}

/**
 * Checks if a CatalogRecord has an acceptable primary topic.
 * 
 * This is a helper function used for filtering CatalogRecords found in the graph.
 * Most graphs will have at least one extra CatalogRecord that supplies metadata
 * about the harvested Catalog/Collection.
 * 
 * @param recordResource The Jena resource of the FDK record
 * @param acceptableTypes A list of RDF types that are acceptable as primary topic
 * @return true if the record has an acceptable primary topic, false otherwise
 * @see primaryTopicFromFdkRecord
 */
private fun hasAcceptablePrimaryTopic(recordResource: Resource, acceptableTypes: List<Resource>): Boolean = try {
    primaryTopicFromFdkRecord(recordResource, acceptableTypes)
    true
} catch (ex: Exception) {
    false
}

/**
 * Determines if a CatalogRecord follows the pattern of FDK harvest records.
 * 
 * This function validates that a CatalogRecord is a valid FDK record by checking:
 * - It is a URI resource (not a blank node)
 * - Its URI contains the FDK pattern
 * - It has an acceptable primary topic
 * 
 * @param recordResource The Jena resource of the FDK record
 * @param acceptableTypes A list of RDF types that are acceptable as primary topic
 * @param fdkURIPattern A pattern that FDK record URIs must contain
 * @return true if the record is a valid FDK record, false otherwise
 * @see hasAcceptablePrimaryTopic
 */
private fun isValidFDKRecord(recordResource: Resource, acceptableTypes: List<Resource>, fdkURIPattern: String): Boolean =
    when {
        !recordResource.isURIResource -> false // All FDK records are URI resources.
        !recordResource.uri.contains(fdkURIPattern) -> false // Checks that the URI follows the FDK pattern.
        !hasAcceptablePrimaryTopic(recordResource, acceptableTypes) -> false // Checks that it's possible to extract the primary topic from the record.
        else -> true
    }

/**
 * Constructs HarvestMetaData from FDK harvest metadata.
 * 
 * This function extracts harvest metadata (modified date and first harvested date)
 * from an FDK record and creates a HarvestMetaData object.
 * 
 * @param recordResource The Jena resource of the FDK record
 * @return HarvestMetaData object with harvest information, or null if no harvest data found
 * @see Resource.extractStringValue
 */
fun harvestMetaData(recordResource: Resource): HarvestMetaData? {
    val builder = HarvestMetaData.newBuilder()

    val harvest = builder.setModified(recordResource.extractStringValue(DCTerms.modified))
        .setFirstHarvested(recordResource.extractStringValue(DCTerms.issued))
        .build()

    return if (harvest.modified != null || harvest.firstHarvested != null) harvest else null
}
