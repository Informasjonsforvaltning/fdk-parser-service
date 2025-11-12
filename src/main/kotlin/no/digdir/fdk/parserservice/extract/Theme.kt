package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.EuDataTheme
import no.digdir.fdk.model.Eurovoc
import no.digdir.fdk.model.LosNode
import no.digdir.fdk.parserservice.vocabulary.FDK
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.SKOS

private val dataThemeBaseURL = "http://publications.europa.eu/resource/authority/data-theme"
private val eurovocBaseURL = "http://eurovoc.europa.eu"
private val losBaseURL = "https://psi.norge.no/los"
private val losThemeUrlPart = "/tema/"

private fun codeFromThemeURI(uri: String?): String? = uri?.split("/")?.last()

fun isEuDataThemeURI(uri: String): Boolean = uri.contains(dataThemeBaseURL)

private fun EuDataTheme.hasContent(): Boolean =
    when {
        uri != null -> true
        code != null -> true
        title != null -> true
        else -> false
    }

/**
 * Extracts EU data theme metadata (code, URI and labels) from the resource.
 *
 * @return populated `EuDataTheme` or `null` when the resource lacks theme content
 */
fun Resource.extractEuDataTheme(): EuDataTheme? {
    val builder = EuDataTheme.newBuilder()

    val themeURI = extractURIStringValue()

    val theme =
        builder
            .setUri(themeURI)
            .setCode(codeFromThemeURI(themeURI))
            .setTitle(extractLocalizedStrings(SKOS.prefLabel))
            .build()

    return if (theme.hasContent()) theme else null
}

fun isEurovocURI(uri: String): Boolean = uri.contains(eurovocBaseURL)

private fun Eurovoc.hasContent(): Boolean =
    when {
        uri != null -> true
        code != null -> true
        label != null -> true
        eurovocPaths != null -> true
        else -> false
    }

/**
 * Extracts Eurovoc concept information including code, labels and stored theme paths.
 *
 * @return populated `Eurovoc` instance or `null` when the concept lacks data
 */
fun Resource.extractEurovoc(): Eurovoc? {
    val builder = Eurovoc.newBuilder()

    val themeURI = if (isURIResource) uri else null

    val theme =
        builder
            .setUri(themeURI)
            .setCode(codeFromThemeURI(themeURI))
            .setLabel(extractLocalizedStrings(SKOS.prefLabel))
            .setEurovocPaths(extractListOfStrings(FDK.themePath))
            .build()

    return if (theme.hasContent()) theme else null
}

fun isLosURI(uri: String): Boolean = uri.contains(losBaseURL)

private fun LosNode.hasContent(): Boolean =
    when {
        uri != null -> true
        code != null -> true
        name != null -> true
        losPaths != null -> true
        else -> false
    }

/**
 * Extracts LOS theme metadata (code, labels, theme paths and tema flag) from the resource.
 *
 * @return populated `LosNode` or `null` when insufficient data is present
 */
fun Resource.extractLosNode(): LosNode? {
    val builder = LosNode.newBuilder()

    val themeURI = if (isURIResource) uri else null

    val theme =
        builder
            .setUri(themeURI)
            .setCode(codeFromThemeURI(themeURI))
            .setName(extractLocalizedStrings(SKOS.prefLabel))
            .setLosPaths(extractListOfStrings(FDK.themePath))
            .setIsTema(themeURI?.contains(losThemeUrlPart))
            .build()

    return if (theme.hasContent()) theme else null
}
