package no.digdir.fdk.parserservice.extract.fdk

import no.digdir.fdk.model.HarvestMetaData
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.singleObjectStatement
import no.digdir.fdk.parserservice.model.MultipleFDKRecordsException
import no.digdir.fdk.parserservice.model.NoAcceptableFDKRecordsException
import no.digdir.fdk.parserservice.model.NoResourceFoundException
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import java.net.URI

/**
 * Extracts a valid FDK record from an RDF model.
 *
 * This function searches for DCAT CatalogRecord resources in the model that has
 * the relevant resource as the primary topic and filters
 * them to find valid FDK records based on URI pattern and acceptable types.
 *
 * @param resource The Jena RDF Resource containing the graph
 * @param fdkId The relevant fdK ID
 * @return The valid FDK record resource
 * @throws Exception if no acceptable FDK records are found
 * @throws Exception if multiple FDK records are found
 * @see Model.listResourcesWithProperty
 * @see isValidFDKRecord
 */
fun fdkRecord(resource: Resource, fdkId: String): Resource {
    val records = resource.model.listResourcesWithProperty(FOAF.primaryTopic, resource)
        .asSequence()
        .filter { isValidFDKRecord(it, fdkId) }
        .toList()

    return when {
        records.isEmpty() -> throw NoAcceptableFDKRecordsException("No acceptable FDK records found in graph")
        records.size > 1 -> throw MultipleFDKRecordsException("Multiple FDK records found in graph")
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
 * Extracts the IRI of the primary topic from a record with identifier.
 *
 * @param id The identifier value of the relevant record
 * @param model The Jena RDF model containing the graph
 * @return The IRI of the primary topic in the record
 */
fun topicUriOfRecordWithID(id: String, model: Model) : String? {
    return model.listSubjectsWithProperty(DCTerms.identifier, id)
        .asSequence()
        .filter { it.isURIResource }
        .firstOrNull()
        ?.getPropertyResourceValue(FOAF.primaryTopic)
        ?.uri
}

/**
 * Extracts a resource by an identifying URI.
 *
 * @param model The Jena RDF model containing the graph
 * @param iri The URI identifying the resource
 * @return The resource identified by the IRI
 * @throws Exception if no resource is found
 * @see Model.getResource
 */
fun resourceOfIRI(model: Model, iri: String): Resource {
    return model.getResource(iri)
        ?.takeIf { it.isURIResource }
        ?: throw NoResourceFoundException("No resource found with uri $iri")
}

/**
 * Determines if a CatalogRecord follows the pattern of FDK harvest records.
 * 
 * This function validates that a CatalogRecord is a valid FDK record by checking:
 * - It is a URI resource (not a blank node)
 * - It has RDF type dcat:CatalogRecord
 * - Its URI contains the FDK pattern
 * - It has an acceptable primary topic
 * 
 * @param recordResource The Jena resource of the FDK record
 * @param fdkId The relevant FDK ID
 * @return true if the record is a valid FDK record, false otherwise
 */
private fun isValidFDKRecord(recordResource: Resource, fdkId: String): Boolean =
    when {
        !recordResource.isURIResource -> false // All FDK records are URI resources.
        !recordResource.model.containsTriple(recordResource.uri, DCTerms.identifier.uri, fdkId) -> false // Checks that the record has the correct identifier value.
        !recordResource.model.containsTriple(recordResource.uri, RDF.type.uri, URI.create(DCAT.CatalogRecord.uri)) -> false // All FDK records has type dcat:CatalogRecord.
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
