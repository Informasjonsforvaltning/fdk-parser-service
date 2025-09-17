package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.dataset.Distribution
import no.digdir.fdk.parseservice.extract.extractFormat
import no.digdir.fdk.parseservice.extract.extractListOfFormats
import no.digdir.fdk.parseservice.extract.extractListOfStrings
import no.digdir.fdk.parseservice.extract.extractListOfUriWithLabel
import no.digdir.fdk.parseservice.extract.extractListOfUriWithLabelAndType
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.extractURIStringValue
import no.digdir.fdk.parseservice.extract.listResources
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

private fun Distribution.hasContent() = when {
    uri != null -> true
    title != null -> true
    description != null -> true
    accessURL != null -> true
    downloadURL != null -> true
    license != null -> true
    conformsTo != null -> true
    page != null -> true
    fdkFormat != null -> true
    compressFormat != null -> true
    packageFormat != null -> true
    accessService != null -> true
    else -> false
}

private fun Resource.addCommonDistributionValuesToBuilder(builder: Distribution.Builder) {
    builder.setUri(extractURIStringValue())
        .setTitle(extractLocalizedStrings(DCTerms.title))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setAccessURL(extractListOfStrings(DCAT.accessURL))
        .setDownloadURL(extractListOfStrings(DCAT.downloadURL))
        .setLicense(extractListOfUriWithLabelAndType(DCTerms.license, DCTerms.source, SKOS.prefLabel))
        .setConformsTo(extractListOfUriWithLabel(DCTerms.conformsTo, DCTerms.source, DCTerms.title))
        .setPage(extractListOfUriWithLabelAndType(FOAF.page, DCTerms.source, SKOS.prefLabel))
}

private fun Resource.buildDistributionV1(): Distribution? {
    val builder = Distribution.newBuilder()

    addCommonDistributionValuesToBuilder(builder)

    builder.setFdkFormat(extractListOfFormats(DCTerms.format))

    // The following properties are not implemented in DCAT-AP-NO v1.1
    builder.setCompressFormat(null)
        .setPackageFormat(null)
        .setAccessService(null)

    return builder.build().takeIf { it.hasContent() }
}

private fun Resource.buildDistributionV2(): Distribution? {
    val builder = Distribution.newBuilder()

    addCommonDistributionValuesToBuilder(builder)

    val formats = extractListOfFormats(DCTerms.format) ?: emptyList()
    val mediaTypes = extractListOfFormats(DCAT.mediaType) ?: emptyList()
    val allFormats = formats + mediaTypes

    builder.setFdkFormat(allFormats.takeIf { it.isNotEmpty() })
        .setCompressFormat(extractFormat(DCAT.compressFormat))
        .setPackageFormat(extractFormat(DCAT.packageFormat))
        .setAccessService(extractListOfAccessServices())

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.extractListOfDistributionsV1(mainPredicate: Property): List<Distribution>? =
    listResources(mainPredicate)
        ?.mapNotNull { it.buildDistributionV1() }
        ?.takeIf { it.isNotEmpty() }

fun Resource.extractListOfDistributionsV2(mainPredicate: Property): List<Distribution>? =
    listResources(mainPredicate)
        ?.mapNotNull { it.buildDistributionV2() }
        ?.takeIf { it.isNotEmpty() }
