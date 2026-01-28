package no.digdir.fdk.parserservice.extract.concept

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.extract.singleResource
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.SKOSXL

/**
 * Extracts the preferred label for a concept using SKOS-XL.
 *
 * This method is used for SKOS-AP-NO v1.1.1 compatibility.
 *
 * @receiver The RDF resource representing the concept
 * @return The preferred label as `LocalizedStrings`, or `null` if not found
 * @see SKOSXL.prefLabel
 * @see SKOSXL.literalForm
 */
fun Resource.extractPrefLabelV1(): LocalizedStrings? =
    singleResource(SKOSXL.prefLabel)
        ?.extractLocalizedStrings(SKOSXL.literalForm)

/**
 * Extracts labels for a concept using SKOS-XL.
 *
 * This method is used for SKOS-AP-NO v1.1.1 compatibility.
 *
 * @receiver The RDF resource representing the concept
 * @param predicate The SKOS-XL predicate to extract labels from (e.g., SKOSXL.altLabel)
 * @return A list of labels as `LocalizedStrings`, or `null` if no labels are found
 * @see SKOSXL.literalForm
 */
fun Resource.extractLabelsV1(predicate: Property): List<LocalizedStrings>? =
    listResources(predicate)
        ?.mapNotNull { it.extractLocalizedStrings(SKOSXL.literalForm) }
        ?.takeIf { it.isNotEmpty() }
