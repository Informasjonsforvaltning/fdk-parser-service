package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.Publisher
import no.digdir.fdk.parseservice.namespace.FDKORG
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.ROV

fun Resource.extractPublisher(): Publisher? {
    val publisherResource = singleObjectStatement(DCTerms.publisher)
        ?.resource
        ?.takeIf { it.isResource }

    if (publisherResource == null) return null
    else {
        val builder = Publisher.newBuilder()

        builder.setUri(if (publisherResource.isURIResource) publisherResource.uri else null)
            .setId(publisherResource.extractStringValue(DCTerms.identifier))
            .setName(publisherResource.extractStringValue(ROV.legalName))
            .setOrgPath(publisherResource.extractStringValue(FDKORG.orgPath))
            .setOrganisasjonsform(publisherResource.extractOrgForm())
            .setPrefLabel(publisherResource.extractLocalizedStrings(FOAF.name))

        return builder.build()
    }
}

private fun Resource.extractOrgForm(): String? =
    extractStringValue(ROV.orgType)
        ?.split("#")
        ?.last()
