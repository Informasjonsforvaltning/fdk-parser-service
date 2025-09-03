package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.LocalizedStrings
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource

/**
 * Extension function to extract localized strings from an RDF resource.
 * 
 * This function extracts all string values with language tags for a given predicate
 * and creates a LocalizedStrings object with the appropriate language mappings.
 * 
 * ## Usage Example
 * 
 * ```kotlin
 * val resource = model.getResource("http://example.org/dataset")
 * val title = resource.extractLocalizedStrings(DCTerms.title)
 * 
 * // Access localized values
 * println("Norwegian title: ${title?.no}")
 * println("English title: ${title?.en}")
 * ```
 * 
 * @param pred The property predicate to search for
 * @return LocalizedStrings object with language-specific values, or null if no values found
 * @see Statement.extractStringLanguagePair
 */
fun Resource.extractLocalizedStrings(pred: Property): LocalizedStrings? {
    val values = listProperties(pred)
        .asSequence()
        .mapNotNull { it.extractStringLanguagePair() }
        .toMap()

    val builder = LocalizedStrings.newBuilder()

    return if (values.isEmpty()) null
    else builder.setNb(values["nb"] ?: values[""])
        .setNn(values["nn"])
        .setNo(values["no"])
        .setEn(values["en"])
        .build()
}

/**
 * Extension function to clean HTML tags from localized strings.
 * 
 * This function removes HTML tags and HTML entities from all language variants
 * in the LocalizedStrings object, creating a clean text version.
 * 
 * ## Usage Example
 * 
 * ```kotlin
 * val description = LocalizedStrings().apply {
 *     no = "<div>Dette er en <strong>beskrivelse</strong></div>"
 *     en = "<p>This is a <em>description</em></p>"
 * }
 * 
 * val cleanDescription = description.descriptionHtmlCleaner()
 * // cleanDescription.no = "Dette er en beskrivelse"
 * // cleanDescription.en = "This is a description"
 * ```
 * 
 * @return New LocalizedStrings object with HTML tags removed
 */
fun LocalizedStrings.descriptionHtmlCleaner(): LocalizedStrings {
    val builder = LocalizedStrings.newBuilder()
    val regex = Regex("<.*?>|&([a-z0-9]+|#[0-9]{1,6}|#x[0-9a-f]{1,6});")

    return builder
        .setNo(no?.replace(regex, ""))
        .setNb(nb?.replace(regex, ""))
        .setNn(nn?.replace(regex, ""))
        .setEn(en?.replace(regex, ""))
        .build()
}

/**
 * Extension function to extract a list of localized strings from an RDF resource.
 * 
 * This function extracts multiple localized string values for a given predicate,
 * creating separate LocalizedStrings objects for each value with its language tag.
 * 
 * @param pred The property predicate to search for
 * @return List of LocalizedStrings objects, or null if no values found
 * @see Statement.extractStringLanguagePair
 */
fun Resource.extractLocalizedStringList(pred: Property): List<LocalizedStrings>? =
    listProperties(pred)
        .asSequence()
        .mapNotNull { it.extractStringLanguagePair() }
        .mapNotNull { pair -> when (pair.first) {
            "no" -> LocalizedStrings().also { it.no = pair.second }
            "nb" -> LocalizedStrings().also { it.nb = pair.second }
            "nn" -> LocalizedStrings().also { it.nn = pair.second }
            "en" -> LocalizedStrings().also { it.en = pair.second }
            else -> null
        } }
        .toList()
        .takeIf { it.isNotEmpty() }
