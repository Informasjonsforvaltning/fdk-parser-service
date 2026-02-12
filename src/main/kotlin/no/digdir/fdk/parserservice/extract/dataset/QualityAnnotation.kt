package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.dataset.QualityAnnotation
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.hasContent
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.extract.singleResource
import no.digdir.fdk.parserservice.vocabulary.DQV
import no.digdir.fdk.parserservice.vocabulary.EULANG
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.OA
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS
import java.net.URI

private fun Resource.buildHasBody(): LocalizedStrings? {
    val hasBodyBuilder =
        LocalizedStrings
            .newBuilder()
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

    return hasBodyBuilder.build().takeIf { it.hasContent() }
}

private fun Resource.buildQualityAnnotationV2(dimension: Resource): QualityAnnotation? {
    val hasBody = buildHasBody()

    return if (hasBody != null) {
        QualityAnnotation
            .newBuilder()
            .setInDimension(dimension.uri)
            .setQualityDimensions(listOf(ReferenceDataCode().apply { uri = dimension.uri }))
            .setHasBody(hasBody)
            .build()
    } else {
        null
    }
}

private fun Resource.buildQualityAnnotation(): QualityAnnotation? {
    val hasBody = buildHasBody()

    return if (hasBody != null) {
        QualityAnnotation
            .newBuilder()
            .setInDimension(singleResource(DQV.inDimension)?.extractURIStringValue())
            .setQualityDimensions(extractListOfReferenceDataCodes(DQV.inDimension, "#", SKOS.prefLabel))
            .setHasBody(hasBody)
            .build()
    } else {
        null
    }
}

/**
 * Extracts the first quality annotation for the supplied dimension (e.g. accuracy,
 * completeness) and maps it to a `QualityAnnotation` containing the hasBody text.
 *
 * @param dimension DQV dimension resource to filter on
 * @return matching `QualityAnnotation` or `null` when none is found
 */
fun Resource.extractQualityAnnotationV2(dimension: Resource): QualityAnnotation? =
    listResources(DQV.hasQualityAnnotation)
        ?.firstOrNull { model.containsTriple(it.uri, DQV.inDimension.uri, URI.create(dimension.uri)) }
        ?.buildQualityAnnotationV2(dimension)

/**
 * Extracts all quality annotations (DQV) associated with a dataset or distribution.
 *
 * @receiver the RDF resource of the dataset to extract quality annotations from
 * @return list of `QualityAnnotation` objects, or `null` if no annotations are found
 */
fun Resource.extractListOfQualityAnnotations(): List<QualityAnnotation>? =
    listResources(DQV.hasQualityAnnotation)
        ?.mapNotNull { it.buildQualityAnnotation() }
        ?.takeIf { it.isNotEmpty() }
