package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.Format
import no.digdir.fdk.model.FormatType
import no.digdir.fdk.parseservice.vocabulary.EUVOC
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.RDF

fun Resource.extractListOfFormats(pred: Property): List<Format>? =
    listProperties(pred)
        .asSequence()
        .mapNotNull { it.buildFormat() }
        .toList()
        .takeIf { it.isNotEmpty() }

private fun Resource.extractType(): FormatType {
    val rdfTypes = listProperties(RDF.type).asSequence()
        .filter { isResource(it) }
        .map { it.resource }
        .toList()

    return when {
        rdfTypes.any { it == DCTerms.MediaType } -> FormatType.MEDIA_TYPE
        rdfTypes.any { it == EUVOC.FileType } -> FormatType.FILE_TYPE
        else -> FormatType.UNKNOWN
    }
}

private fun Statement.buildFormat(): Format? {
    val builder = Format.newBuilder()

    if (isResource(this)) {
        builder
            .setUri(resource.takeIf { it.isURIResource }?.uri)
            .setName(resource.extractStringValue(DCTerms.title) ?: resource.extractStringValue(DC_11.identifier))
            .setCode(resource.extractStringValue(DCTerms.identifier) ?: resource.extractStringValue(DC_11.identifier))
            .setType(resource.extractType())
    } else {
        builder
            .setUri(null)
            .setName(string)
            .setCode(string)
            .setType(FormatType.UNKNOWN)

    }

    return builder.build()
        .takeIf { it.uri != null || it.name != null || it.code != null }
}
