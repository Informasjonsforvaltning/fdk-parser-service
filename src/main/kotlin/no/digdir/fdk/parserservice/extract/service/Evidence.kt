package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.service.ServiceEvidence
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.CPSVNO
import no.digdir.fdk.parserservice.vocabulary.CV
import no.digdir.fdk.parserservice.vocabulary.EUAT
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS

private fun ServiceEvidence.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        rdfType != null -> true
        dctType != null -> true
        name != null -> true
        description != null -> true
        language != null -> true
        page != null -> true
        else -> false
    }

private fun Resource.extractEvidenceRdfType(): String? {
    val types = extractListOfStrings(RDF.type)
    val typeEvidence = types?.firstOrNull { it.contains("${CV.URI}Evidence") }
    val typeDataset = types?.firstOrNull { it.contains("${DCAT.NS}Dataset") }
    val typeRequiredEvidence = types?.firstOrNull { it.contains("${CPSVNO.URI}RequiredEvidence") }

    return when {
        typeRequiredEvidence != null -> typeRequiredEvidence
        typeEvidence != null -> typeEvidence
        typeDataset != null -> typeDataset
        else -> types?.firstOrNull()
    }
}

private fun Resource.buildServiceEvidence(): ServiceEvidence? {
    val builder = ServiceEvidence.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setRdfType(extractEvidenceRdfType())
        .setDctType(extractListOfReferenceDataCodes(DCTerms.type, "#", SKOS.prefLabel))
        .setName(extractLocalizedStrings(DCTerms.title))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setLanguage(extractListOfReferenceDataCodes(DCTerms.language, EUAT.authorityCode, SKOS.prefLabel))
        .setPage(extractListOfStrings(FOAF.page))

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts all V0 service evidence resources and converts them to `ServiceEvidence`
 * objects containing evidence metadata such as name, description, type, language,
 * and page references. Evidence can be of type Evidence or Dataset.
 *
 * This function collects evidence from two sources:
 * 1. Evidence directly associated with the service via the predicate
 * 2. Evidence associated with service channels via the predicate on each channel
 *
 * The results are combined and deduplicated by URI, ensuring each evidence item
 * appears only once even if referenced from multiple sources.
 *
 * @return list of service evidence or `null` when no evidence information exists
 */
fun Resource.extractListOfServiceEvidence(predicate: Property): List<ServiceEvidence>? {
    val allEvidence = mutableListOf(listResources(predicate))
    listResources(CV.hasChannel)
        ?.forEach { allEvidence.add(it.listResources(predicate)) }

    return allEvidence
        .filterNotNull()
        .flatten()
        .distinctBy { if (it.isURIResource) it.uri else it.id }
        .mapNotNull { it.buildServiceEvidence() }
        .takeIf { it.isNotEmpty() }
}
