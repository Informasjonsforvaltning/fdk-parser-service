package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.ReferenceDataCode
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource

private fun ReferenceDataCode.hasContent() =
    when {
        uri != null -> true
        code != null -> true
        prefLabel != null -> true
        else -> false
    }

/**
 * Builds ReferenceDataCode.
 *
 * @param codePredicate The predicate for the code value
 * @param labelPredicate The predicate for the label value
 * @return ReferenceDataCode if able to find any values for the object, null otherwise
 */
private fun Resource.buildReferenceDataCode(
    codePredicate: Property,
    labelPredicate: Property,
): ReferenceDataCode? {
    val builder = ReferenceDataCode.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setCode(extractStringValue(codePredicate))
        .setPrefLabel(extractLocalizedStrings(labelPredicate))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Builds ReferenceDataCode, missing a code predicate and uses a split of the uri instead.
 *
 * @param codeSeparator The separator used to split the code from the URI
 * @param labelPredicate The predicate for the label value
 * @return ReferenceDataCode if able to find any values for the object, null otherwise
 */
private fun Resource.buildReferenceDataCode(
    codeSeparator: String?,
    labelPredicate: Property,
): ReferenceDataCode? {
    val builder = ReferenceDataCode.newBuilder()
    val uri = extractURIStringValue()

    builder
        .setUri(uri)
        .setCode(codeSeparator?.let { separator -> uri?.split(separator)?.lastOrNull() })
        .setPrefLabel(extractLocalizedStrings(labelPredicate))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts values for a resource originating from fdk-reference-data.
 *
 * @param mainPredicate The predicate that points to the reference data from the originating resource
 * @param codePredicate The predicate for the code value
 * @param labelPredicate The predicate for the label value
 * @return ReferenceDataCode if any exists, null otherwise
 */
fun Resource.extractReferenceDataCode(
    mainPredicate: Property,
    codePredicate: Property,
    labelPredicate: Property,
): ReferenceDataCode? =
    singleResource(mainPredicate)
        ?.buildReferenceDataCode(codePredicate, labelPredicate)

/**
 * Extracts values for a resource originating from fdk-reference-data, missing a code predicate.
 *
 * @param mainPredicate The predicate that points to the reference data from the originating resource
 * @param codeSeparator The separator used to split the code from the URI
 * @param labelPredicate The predicate for the label value
 * @return ReferenceDataCode if any exists, null otherwise
 */
fun Resource.extractReferenceDataCode(
    mainPredicate: Property,
    codeSeparator: String?,
    labelPredicate: Property,
): ReferenceDataCode? =
    singleResource(mainPredicate)
        ?.buildReferenceDataCode(codeSeparator, labelPredicate)

/**
 * Extracts values for a list of resources originating from fdk-reference-data.
 *
 * @param mainPredicate The predicate that points to the reference data from the originating resource
 * @param codePredicate The predicate for the code value
 * @param labelPredicate The predicate for the label value
 * @return ReferenceDataCode if any exists, null otherwise
 */
fun Resource.extractListOfReferenceDataCodes(
    mainPredicate: Property,
    codePredicate: Property,
    labelPredicate: Property,
): List<ReferenceDataCode>? =
    listResources(mainPredicate)
        ?.mapNotNull { it.buildReferenceDataCode(codePredicate, labelPredicate) }
        ?.takeIf { it.isNotEmpty() }

/**
 * Extracts values for a list of resources originating from fdk-reference-data, missing a code predicate.
 *
 * @param mainPredicate The predicate that points to the reference data from the originating resource
 * @param codeSeparator The separator used to split the code from the URI
 * @param labelPredicate The predicate for the label value
 * @return ReferenceDataCode if any exists, null otherwise
 */
fun Resource.extractListOfReferenceDataCodes(
    mainPredicate: Property,
    codeSeparator: String?,
    labelPredicate: Property,
): List<ReferenceDataCode>? =
    listResources(mainPredicate)
        ?.mapNotNull { it.buildReferenceDataCode(codeSeparator, labelPredicate) }
        ?.takeIf { it.isNotEmpty() }
