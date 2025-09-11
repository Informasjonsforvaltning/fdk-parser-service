package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.UriWithLabelAndType
import no.digdir.fdk.parseservice.extract.containsTriple
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.extract.isURIResource
import no.digdir.fdk.parseservice.extract.singleResource
import no.digdir.fdk.parseservice.vocabulary.CPSV
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.SKOS
import java.net.URI

fun Resource.extractListOfLegalBasisV2(rule: Property): List<UriWithLabelAndType>? =
    listProperties(CPSV.follows)
        .asSequence()
        .filter { isURIResource(it) }
        .filter { model.containsTriple(it.resource.uri, DCTerms.type.uri, URI.create(rule.uri)) }
        .mapNotNull { it.resource.buildLegalBasisV2(rule) }
        .toList()
        .takeIf { it.isNotEmpty() }

private fun Resource.buildLegalBasisV2(rule: Property): UriWithLabelAndType? {
    val builder = UriWithLabelAndType.newBuilder()

    val implements = singleResource(CPSV.implements)
    val implementsType = implements?.singleResource(DCTerms.type)

    return builder
        .setUri(implements?.extractStringValue(RDFS.seeAlso))
        .setExtraType(rule.uri)
        .setPrefLabel(implementsType?.extractLocalizedStrings(SKOS.prefLabel))
        .build()
        .takeIf { it.uri != null || it.prefLabel != null }
}
