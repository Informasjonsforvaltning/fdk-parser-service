package no.digdir.fdk.parserservice.vocabulary

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
        /**
         * Base URI for the EUVOC vocabulary.
         * 
         * This URI points to the European Union vocabulary namespace.
         */
        const val uri = "http://publications.europa.eu/ontology/euvoc#"

        /**
         * Resource for file type classification.
         * 
         * This resource is used to classify different types of files
         * according to EU vocabulary standards.
         */
        val FileType: Resource = ResourceFactory.createResource("${uri}FileType")
    }

}
