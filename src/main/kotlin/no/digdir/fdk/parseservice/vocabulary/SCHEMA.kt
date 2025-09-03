package no.digdir.fdk.parseservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

/**
 * Vocabulary class for Schema.org properties and URIs.
 * 
 * This class contains constants and properties from the Schema.org vocabulary
 * used for structured data markup and temporal information.
 * 
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 * @see <a href="https://schema.org/">Schema.org</a>
 */
class SCHEMA {

    companion object {
        /**
         * Base URI for the Schema.org vocabulary.
         * 
         * This URI points to the Schema.org vocabulary namespace.
         */
        const val uri = "http://schema.org/"

        /**
         * Property for start date information.
         * 
         * This property is used to specify the start date of temporal periods
         * or events according to Schema.org standards.
         */
        val startDate: Property = ResourceFactory.createProperty("${uri}startDate")
        
        /**
         * Property for end date information.
         * 
         * This property is used to specify the end date of temporal periods
         * or events according to Schema.org standards.
         */
        val endDate: Property = ResourceFactory.createProperty("${uri}endDate")
    }

}
