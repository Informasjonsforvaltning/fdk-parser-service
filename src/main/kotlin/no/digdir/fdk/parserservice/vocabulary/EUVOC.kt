package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

/**
 * Vocabulary class for EUVOC (European Union Vocabulary) resources and URIs.
 *
 * This class contains constants and resources from the European Union
 * vocabulary used for EU-specific concepts and classifications.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
class EUVOC {
    companion object {
        const val URI = "http://publications.europa.eu/ontology/euvoc#"

        val status: Property = ResourceFactory.createProperty("${URI}status")
        val startDate: Property = ResourceFactory.createProperty("${URI}startDate")
        val endDate: Property = ResourceFactory.createProperty("${URI}endDate")
        val xlDefinition: Property = ResourceFactory.createProperty("${URI}xlDefinition")

        val FileType: Resource = ResourceFactory.createResource("${URI}FileType")
    }
}
