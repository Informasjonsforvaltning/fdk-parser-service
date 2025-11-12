package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.Organization
import no.digdir.fdk.parserservice.vocabulary.CV
import no.digdir.fdk.parserservice.vocabulary.FDKORG
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.ORG
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.ROV
import org.apache.jena.vocabulary.SKOS

private fun Organization.hasContent() =
    when {
        uri != null -> true
        id != null -> true
        name != null -> true
        orgPath != null -> true
        organisasjonsform != null -> true
        prefLabel != null -> true
        title != null -> true
        else -> false
    }

/**
 * Extension function to extract organization information from an RDF resource.
 *
 * This function extracts organization metadata including URI, identifier, legal name,
 * organization path, organizational form, and preferred labels from an RDF resource.
 *
 * @param pred The property predicate pointing to the organization resource
 * @return Organization object with extracted metadata, or null if no organization found
 */
fun Resource.extractOrganization(pred: Property): Organization? {
    val orgResource = singleResource(pred)

    return if (orgResource == null) {
        null
    } else {
        orgResource.buildOrganization()
    }
}

/**
 * Extension function to extract a list of organizations from an RDF resource.
 *
 * This function extracts organization metadata including URI, identifier, legal name,
 * organization path, organizational form, and preferred labels from an RDF resource.
 *
 * @param pred The property predicate pointing to the organization resource
 * @return List of organization objects with extracted metadata, or null if no organizations found
 */
fun Resource.extractListOfOrganizations(pred: Property): List<Organization>? =
    listResources(pred)
        ?.mapNotNull { it.buildOrganization() }
        ?.takeIf { it.isNotEmpty() }

private fun Resource.buildOrganization(): Organization? {
    val builder = Organization.newBuilder()

    val types = listResources(RDF.type) ?: emptyList()
    return when {
        types.contains(ROV.RegisteredOrganization) -> buildForNamespaceROV(builder)
        types.contains(ORG.Organization) -> buildForNamespaceORG(builder)
        types.contains(CV.PublicOrganisation) -> buildForNamespaceORG(builder)
        else -> buildForGenericAgent(builder)
    }
}

private fun Resource.buildForNamespaceORG(builder: Organization.Builder): Organization? {
    builder
        .setUri(extractURIStringValue())
        .setId(extractStringValue(DCTerms.identifier))
        .setName(extractStringValue(FOAF.name))
        .setOrgPath(extractStringValue(FDKORG.orgPath))
        .setOrganisasjonsform(extractOrgForm())
        .setPrefLabel(extractLocalizedStrings(SKOS.prefLabel))
        .setTitle(extractLocalizedStrings(DCTerms.title))

    return builder.build().takeIf { it.hasContent() }
}

private fun Resource.buildForNamespaceROV(builder: Organization.Builder): Organization? {
    builder
        .setUri(extractURIStringValue())
        .setId(extractStringValue(DCTerms.identifier))
        .setName(extractStringValue(ROV.legalName))
        .setOrgPath(extractStringValue(FDKORG.orgPath))
        .setOrganisasjonsform(extractOrgForm())
        .setPrefLabel(extractLocalizedStrings(FOAF.name))
        .setTitle(extractLocalizedStrings(FOAF.name))

    return builder.build().takeIf { it.hasContent() }
}

private fun Resource.buildForGenericAgent(builder: Organization.Builder): Organization? {
    builder
        .setUri(extractURIStringValue())
        .setId(extractStringValue(DCTerms.identifier))
        .setName(extractStringValue(FOAF.name))
        .setOrgPath(extractStringValue(FDKORG.orgPath))
        .setOrganisasjonsform(null)
        .setPrefLabel(extractLocalizedStrings(FOAF.name))
        .setTitle(extractLocalizedStrings(FOAF.name))

    return builder.build().takeIf { it.hasContent() }
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
