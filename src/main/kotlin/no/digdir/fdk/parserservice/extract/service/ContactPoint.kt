package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.service.ServiceContactPoint
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.CV
import no.digdir.fdk.parserservice.vocabulary.EUAT
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.SKOS
import org.apache.jena.vocabulary.VCARD4

private fun ServiceContactPoint.hasContent() =
    when {
        uri != null -> true
        contactType != null -> true
        email != null -> true
        telephone != null -> true
        contactPage != null -> true
        language != null -> true
        else -> false
    }

private fun Resource.extractTelephone(): List<String>? =
    extractListOfStrings(CV.telephone)
        ?.map { it.removePrefix("tel:") }
        ?.filter { it.isNotBlank() }
        ?.takeIf { it.isNotEmpty() }

private fun Resource.extractEmail(): List<String>? =
    extractListOfStrings(CV.email)
        ?.map { it.removePrefix("mailto:") }
        ?.filter { it.isNotBlank() }
        ?.takeIf { it.isNotEmpty() }

private fun Resource.buildServiceContactPoint(): ServiceContactPoint? {
    val builder = ServiceContactPoint.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setContactType(extractLocalizedStrings(VCARD4.category))
        .setEmail(extractEmail())
        .setTelephone(extractTelephone())
        .setContactPage(extractListOfStrings(CV.contactPage))
        .setLanguage(extractListOfReferenceDataCodes(VCARD4.language, EUAT.authorityCode, SKOS.prefLabel))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts all DCAT contact point resources and converts them to `ContactPoint`
 * objects containing vCard metadata such as formatted name, email and phone number.
 *
 * @return list of contact points or `null` when no contact point information exists
 */
fun Resource.extractListOfServiceContactPoints(): List<ServiceContactPoint>? =
    listResources(DCAT.contactPoint)
        ?.mapNotNull { it.buildServiceContactPoint() }
        ?.takeIf { it.isNotEmpty() }
