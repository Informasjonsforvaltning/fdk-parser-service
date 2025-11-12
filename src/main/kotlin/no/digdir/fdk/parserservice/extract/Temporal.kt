package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.Temporal
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource

private fun Temporal.hasContent() =
    when {
        uri != null -> true
        startDate != null -> true
        endDate != null -> true
        else -> false
    }

private fun Resource.buildTemporal(
    startPredicate: Property,
    endPredicate: Property,
): Temporal? {
    val builder = Temporal.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setStartDate(extractStringValue(startPredicate))
        .setEndDate(extractStringValue(endPredicate))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts temporal coverage intervals by following the supplied predicate and reading
 * start/end dates from the referenced resources.
 *
 * @param mainPredicate predicate linking to temporal resources
 * @param startPredicate predicate holding the start date literal
 * @param endPredicate predicate holding the end date literal
 * @return list of `Temporal` objects or `null` when none exist
 */
fun Resource.extractListOfTemporal(
    mainPredicate: Property,
    startPredicate: Property,
    endPredicate: Property,
): List<Temporal>? =
    listResources(mainPredicate)
        ?.mapNotNull { it.buildTemporal(startPredicate, endPredicate) }
        ?.takeIf { it.isNotEmpty() }
