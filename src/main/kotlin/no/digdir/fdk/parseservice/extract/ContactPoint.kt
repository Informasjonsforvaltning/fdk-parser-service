package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.ContactPoint
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.VCARD
import org.apache.jena.vocabulary.VCARD4

private fun ContactPoint.hasContent() = when {
    uri != null -> true
    fullname != null -> true
    email != null -> true
    hasURL != null -> true
    hasTelephone != null -> true
    organizationName != null -> true
    organizationUnit != null -> true
    else -> false
}

private fun Resource.buildContactPoint(): ContactPoint? {
    val builder = ContactPoint.newBuilder()

    builder.setUri(if (isURIResource) uri else null)
        .setFullname(extractStringValue(VCARD4.fn))
        .setEmail(extractStringValue(VCARD4.hasEmail))
        .setHasURL(extractStringValue(VCARD4.hasURL))
        .setHasTelephone(extractStringValue(VCARD4.hasTelephone))
        .setOrganizationName(extractLocalizedStrings(VCARD4.hasOrganizationName))
        .setOrganizationUnit(extractLocalizedStrings(VCARD4.organization_unit))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.extractListOfContactPoints(): List<ContactPoint>? =
    listResources(DCAT.contactPoint)
        ?.mapNotNull { it.buildContactPoint() }
        ?.takeIf { it.isNotEmpty() }
