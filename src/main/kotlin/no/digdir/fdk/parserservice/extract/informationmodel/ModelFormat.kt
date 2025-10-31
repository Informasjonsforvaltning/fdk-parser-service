package no.digdir.fdk.parserservice.extract.informationmodel

import no.digdir.fdk.model.informationmodel.InformationModelFormat
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractReferenceDataCode
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.isResource
import no.digdir.fdk.parserservice.vocabulary.EUAT
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDFS
import org.apache.jena.vocabulary.SKOS

private fun InformationModelFormat.hasContent(): Boolean =
    when {
        uri != null -> true
        title != null -> true
        seeAlso != null -> true
        format != null -> true
        language != null -> true
        else -> false
    }

fun Resource.extractListOfModelFormat(): List<InformationModelFormat>? =
    listProperties(DCTerms.hasFormat)
        .asSequence()
        .mapNotNull { it.buildModelFormat() }
        .toList()
        .takeIf { it.isNotEmpty() }

private fun Statement.buildModelFormat(): InformationModelFormat? {
    if (isResource(this)) {
        val builder = InformationModelFormat.newBuilder()

        return builder
            .setUri(resource.extractURIStringValue())
            .setTitle(resource.extractLocalizedStrings(DCTerms.title))
            .setSeeAlso(resource.extractStringValue(RDFS.seeAlso))
            .setFormat(resource.extractStringValue(DCTerms.format))
            .setLanguage(resource.extractReferenceDataCode(DCTerms.language, EUAT.authorityCode, SKOS.prefLabel))
            .build()
            .takeIf { it.hasContent() }
    } else {
        return null
    }
}
