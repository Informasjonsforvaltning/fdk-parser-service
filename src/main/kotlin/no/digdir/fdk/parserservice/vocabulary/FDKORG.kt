package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

/**
 * Vocabulary class for FDK Organization-specific properties and URIs.
 * 
 * This class contains constants and properties specific to the FDK Organization
 * catalog ontology used for organization-related metadata.
 * 
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
class FDKORG {

    companion object {
        /**
         * Base URI for the FDK Organization catalog ontology.
         * 
         * This URI points to the organization catalog ontology maintained
         * by the FDK organization service.
         */
        const val uri =
            "https://raw.githubusercontent.com/Informasjonsforvaltning/organization-catalog/main/src/main/resources/ontology/organization-catalog.owl#"

        /**
         * Property for organization path information.
         * 
         * This property is used to store hierarchical path information
         * for organizations in the Norwegian public sector.
         */
        val orgPath: Property = ResourceFactory.createProperty("${uri}orgPath")
    }

}
