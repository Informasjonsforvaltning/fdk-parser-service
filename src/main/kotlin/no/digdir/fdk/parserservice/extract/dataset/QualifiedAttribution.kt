package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.dataset.QualifiedAttribution
import no.digdir.fdk.parserservice.extract.extractOrganization
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.PROV
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT

private fun QualifiedAttribution.hasContent() =
    when {
        role != null -> true
        agent != null -> true
        else -> false
    }

private fun Resource.buildQualifiedAttribution(): QualifiedAttribution? {
    val builder = QualifiedAttribution.newBuilder()

    builder
        .setAgent(extractOrganization(PROV.agent))
        .setRole(extractStringValue(DCAT.hadRole))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts PROV qualified attribution entries, capturing both the agent (organization)
 * and role applied to the dataset or distribution.
 *
 * @return list of `QualifiedAttribution` items or `null` when none exist
 */
fun Resource.extractListOfQualifiedAttributions(): List<QualifiedAttribution>? =
    listResources(PROV.qualifiedAttribution)
        ?.mapNotNull { it.buildQualifiedAttribution() }
        ?.takeIf { it.isNotEmpty() }
