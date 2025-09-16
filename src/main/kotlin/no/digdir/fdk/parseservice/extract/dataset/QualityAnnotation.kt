package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.dataset.QualityAnnotation
import no.digdir.fdk.parseservice.extract.containsTriple
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.extract.hasContent
import no.digdir.fdk.parseservice.extract.listResources
import no.digdir.fdk.parseservice.vocabulary.DQV
import no.digdir.fdk.parseservice.vocabulary.EULANG
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.OA
import org.apache.jena.vocabulary.RDF
import java.net.URI

private fun Resource.buildQualityAnnotation(dimension: Resource): QualityAnnotation? {
    val hasBodyBuilder = LocalizedStrings.newBuilder()
        .setNo(null)
        .setNb(null)
        .setNn(null)
        .setEn(null)

    listResources(OA.hasBody)
        ?.asSequence()
        ?.forEach {
            val lang = it.extractStringValue(DCTerms.language)
            val value = it.extractStringValue(RDF.value)
            if (value?.isNotBlank() == true) {
                when (lang) {
                    EULANG.NOR.uri -> hasBodyBuilder.setNo(value)
                    EULANG.NOB.uri -> hasBodyBuilder.setNb(value)
                    EULANG.NNO.uri -> hasBodyBuilder.setNn(value)
                    EULANG.ENG.uri -> hasBodyBuilder.setEn(value)
                    else -> hasBodyBuilder.setNo(value)
                }
            }
        }

    val hasBody = hasBodyBuilder.build()

    return if (hasBody.hasContent()) {
        QualityAnnotation.newBuilder()
            .setInDimension(dimension.uri)
            .setHasBody(hasBody)
            .build()
    } else null
}

fun Resource.extractQualityAnnotation(dimension: Resource): QualityAnnotation? =
    listResources(DQV.hasQualityAnnotation)
        ?.firstOrNull { model.containsTriple(it.uri, DQV.inDimension.uri, URI.create(dimension.uri)) }
        ?.buildQualityAnnotation(dimension)
