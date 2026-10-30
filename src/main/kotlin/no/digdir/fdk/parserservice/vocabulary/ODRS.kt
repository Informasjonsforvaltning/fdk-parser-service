package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

/**
 * Vocabulary class for ODRS (Open Data Rights Statement Vocabulary) properties and URIs.
 *
 * @see <a href="http://schema.theodi.org/odrs/">ODRS Vocabulary</a>
 */
class ODRS {
    companion object {
        /**
         * Base URI for the ODRS vocabulary.
         */
        const val URI = "http://schema.theodi.org/odrs#"

        /**
         * Property for links to guidelines for re-users of data covered by a rights statement.
         */
        val reuserGuidelines: Property = ResourceFactory.createProperty("${URI}reuserGuidelines")
    }
}
