package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

/**
 * Vocabulary class for EU Authority properties and URIs.
 *
 * This class contains constants and properties from the European Union
 * authority vocabulary used for referencing EU-controlled vocabularies
 * and authority codes.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
class EUAT {
    companion object {
        /**
         * Base URI for the EU Authority vocabulary.
         *
         * This URI points to the European Union authority vocabulary namespace.
         */
        const val URI = "http://publications.europa.eu/ontology/authority/"

        /**
         * Property for authority codes.
         *
         * This property is used to specify authority codes for EU-controlled
         * vocabularies and reference data.
         */
        val authorityCode: Property = ResourceFactory.createProperty("${URI}authority-code")
    }
}
