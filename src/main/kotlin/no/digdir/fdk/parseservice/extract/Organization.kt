package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.Organization
import no.digdir.fdk.parseservice.vocabulary.FDKORG
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.ROV

/**
 * Extension function to extract organization information from an RDF resource.
 * 
 * This function extracts organization metadata including URI, identifier, legal name,
 * organization path, organizational form, and preferred labels from an RDF resource.
 * 
 * @param pred The property predicate pointing to the organization resource
 * @return Organization object with extracted metadata, or null if no organization found
 * @see Resource.singleResource
 * @see Resource.extractStringValue
 * @see Resource.extractLocalizedStrings
 */
fun Resource.extractOrganization(pred: Property): Organization? {
    val orgResource = singleResource(pred)

    if (orgResource == null) return null
    else {
        val builder = Organization.newBuilder()

        builder.setUri(if (orgResource.isURIResource) orgResource.uri else null)
            .setId(orgResource.extractStringValue(DCTerms.identifier))
            .setName(orgResource.extractStringValue(ROV.legalName))
            .setOrgPath(orgResource.extractStringValue(FDKORG.orgPath))
            .setOrganisasjonsform(orgResource.extractOrgForm())
            .setPrefLabel(orgResource.extractLocalizedStrings(FOAF.name))

        return builder.build()
    }
}

/**
 * Private extension function to extract organizational form from an RDF resource.
 * 
 * This function extracts the organizational form from the ROV.orgType property
 * and returns the last part of the URI (after the # character).
 * 
 * @return The organizational form code, or null if not found
 * @see Resource.extractStringValue
 */
private fun Resource.extractOrgForm(): String? =
    extractStringValue(ROV.orgType)
        ?.split("#")
        ?.last()
