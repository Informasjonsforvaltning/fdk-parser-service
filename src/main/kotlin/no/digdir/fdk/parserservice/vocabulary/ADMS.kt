package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

/**
 * Vocabulary class for ADMS (Asset Description Metadata Schema) properties and URIs.
 * 
 * This class contains constants and properties from the W3C ADMS vocabulary
 * used for describing assets and their metadata.
 * 
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 * @see <a href="https://www.w3.org/TR/vocab-adms/">ADMS Vocabulary</a>
 */
class ADMS {

    companion object {
        /**
         * Base URI for the ADMS vocabulary.
         * 
         * This URI points to the W3C ADMS vocabulary namespace.
         */
        const val uri =
            "http://www.w3.org/ns/adms#"

        /**
         * Property for asset identifiers.
         * 
         * This property is used to specify identifiers for assets
         * according to the ADMS specification.
         */
        val identifier: Property = ResourceFactory.createProperty("${uri}identifier")
        
        /**
         * Property for asset samples.
         * 
         * This property is used to link to sample data or examples
         * of the asset being described.
         */
        val sample: Property = ResourceFactory.createProperty("${uri}sample")

        /**
         * Property for asset status.
         */
        val status: Property = ResourceFactory.createProperty("${uri}status")
    }

}
