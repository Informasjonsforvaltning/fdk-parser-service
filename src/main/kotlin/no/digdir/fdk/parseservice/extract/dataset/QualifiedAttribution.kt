package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.dataset.QualifiedAttribution
import no.digdir.fdk.parseservice.extract.extractOrganization
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.extract.listResources
import no.digdir.fdk.parseservice.vocabulary.PROV
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT

private fun QualifiedAttribution.hasContent() = when {
    role != null -> true
    agent != null -> true
    else -> false
}

private fun Resource.buildQualifiedAttribution(): QualifiedAttribution? {
    val builder = QualifiedAttribution.newBuilder()

    builder.setAgent(extractOrganization(PROV.agent))
        .setRole(extractStringValue(DCAT.hadRole))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.extractListOfQualifiedAttributions(): List<QualifiedAttribution>? =
    listResources(PROV.qualifiedAttribution)
        ?.mapNotNull { it.buildQualifiedAttribution() }
        ?.takeIf { it.isNotEmpty() }
