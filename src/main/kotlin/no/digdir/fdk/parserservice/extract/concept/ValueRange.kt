package no.digdir.fdk.parserservice.extract.concept

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.UriWithText
import no.digdir.fdk.parserservice.extract.extractStringLanguagePair
import no.digdir.fdk.parserservice.extract.isResource
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.vocabulary.SKOSNO
import org.apache.jena.rdf.model.Resource

/**
 * Extracts value range information from a concept resource.
 *
 * The function handles two types of value ranges:
 * - **URI-based ranges:** When the value range is a resource (URI), it extracts
 *   the URI and creates a [UriWithText] object with the URI and null text.
 * - **Literal-based ranges:** When the value range is a literal value with language
 *   tags, it extracts the localized text and creates a [UriWithText] object with
 *   null URI and localized text in the appropriate language (no, nb, nn, or en).
 *
 * @receiver The RDF resource representing the concept
 * @return A list of `UriWithText` if any value ranges are found, `null` if not.
 *
 * @see SKOSNO.valueRange
 * @see UriWithText
 */
fun Resource.extractValueRange(): List<UriWithText>? {
    val ranges = mutableListOf<UriWithText>()
    listProperties(SKOSNO.valueRange)?.forEach { stmt ->
        if (isResource(stmt)) {
            ranges.add(
                UriWithText
                    .newBuilder()
                    .setUri(stmt.resource.uri)
                    .setText(null)
                    .build(),
            )
        } else if (stmt.literal != null) {
            val value = stmt.extractStringLanguagePair()
            val txt =
                LocalizedStrings
                    .newBuilder()
                    .setNo(if (value?.first == LanguageCodes.NORWEGIAN || value?.first == LanguageCodes.NONE) value.second else null)
                    .setNb(if (value?.first == LanguageCodes.NORWEGIAN_BOKMAL) value.second else null)
                    .setNn(if (value?.first == LanguageCodes.NORWEGIAN_NYNORSK) value.second else null)
                    .setEn(if (value?.first == LanguageCodes.ENGLISH) value.second else null)
                    .build()
            ranges.add(
                UriWithText
                    .newBuilder()
                    .setUri(null)
                    .setText(txt)
                    .build(),
            )
        }
    }

    return ranges.takeIf { it.isNotEmpty() }
}
