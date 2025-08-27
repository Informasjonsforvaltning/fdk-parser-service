package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.Organization
import no.digdir.fdk.parseservice.namespace.FDKORG
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.ROV

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

private fun Resource.extractOrgForm(): String? =
    extractStringValue(ROV.orgType)
        ?.split("#")
        ?.last()
