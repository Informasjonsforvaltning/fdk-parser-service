package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

/**
 * Vocabulary class for FDK-specific properties and URIs.
 *
 * This class contains constants and properties specific to the Felles Datakatalog (FDK)
 * ontology and internal vocabulary used by the FDK reasoning service.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
class FDK {
    companion object {
        /**
         * Base URI for the FDK ontology.
         *
         * This URI points to the FDK ontology maintained by the reasoning service.
         */
        const val URI =
            "https://raw.githubusercontent.com/Informasjonsforvaltning/fdk-reasoning-service/main/src/main/resources/ontology/fdk.owl#"

        /**
         * Property indicating whether a dataset is authoritative.
         *
         * This property is added by the FDK reasoning service to indicate
         * if a dataset comes from an authoritative source.
         */
        val isAuthoritative: Property = ResourceFactory.createProperty("${URI}isAuthoritative")

        /**
         * Property indicating whether a dataset is related to the transport portal.
         *
         * This property is used to mark datasets that are relevant to
         * transportation and mobility information.
         */
        val isRelatedToTransportportal: Property = ResourceFactory.createProperty("${URI}isRelatedToTransportportal")

        /**
         * Property indicating whether a dataset is open data.
         *
         * This property is added by the FDK reasoning service to indicate
         * if a dataset meets the criteria for open data.
         */
        val isOpenData: Property = ResourceFactory.createProperty("${URI}isOpenData")

        /**
         * Base URI for internal FDK vocabulary.
         *
         * This URI is used for internal properties and concepts
         * specific to the FDK implementation.
         */
        const val INTERNAL_URI = "https://fellesdatakatalog.digdir.no/ontology/internal/"

        /**
         * Property for theme path information.
         *
         * This property is used to store hierarchical path information
         * for themes and classifications.
         */
        val themePath: Property = ResourceFactory.createProperty("${INTERNAL_URI}themePath")
    }
}
