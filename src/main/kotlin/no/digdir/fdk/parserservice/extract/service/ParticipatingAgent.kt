package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.service.ServiceAgent
import no.digdir.fdk.model.service.ServiceParticipation
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

private fun ServiceParticipation.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        description != null -> true
        role != null -> true
        agent != null -> true
        else -> false
    }

private fun Resource.buildServiceParticipation(): ServiceParticipation? {
    val builder = ServiceParticipation.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setRole(extractListOfReferenceDataCodes(CV.role, DCTerms.identifier, SKOS.prefLabel))
        .setAgent(extractStringValue(CV.hasParticipant))

    return builder.build().takeIf { it.hasContent() }
}

private fun ServiceAgent.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        name != null -> true
        playsRole != null -> true
        else -> false
    }

private fun Resource.buildServiceAgent(participation: List<ServiceParticipation>?): ServiceAgent? {
    val builder = ServiceAgent.newBuilder()

    val uri = extractURIStringValue()

    builder
        .setUri(uri)
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setName(extractLocalizedStrings(DCTerms.title))
        .setPlaysRole(participation?.filter { it.agent == uri }?.takeIf { it.isNotEmpty() })

    return builder.build().takeIf { it.hasContent() }
}

/**
 * Extracts participating service agents and their roles by combining participation
 * resources (`cv:hasParticipation`) with the referenced agent nodes.
 *
 * @return list of `ServiceAgent` entries or `null` when no participation is defined
 */
fun Resource.extractListOfParticipatingAgents(): List<ServiceAgent>? {
    val participationResources = listResources(CV.hasParticipation)
    val agentResources: List<Resource>? =
        participationResources
            ?.mapNotNull { it.listResources(CV.hasParticipant) }
            ?.flatten()
    val participation = participationResources?.mapNotNull { it.buildServiceParticipation() }

    return agentResources
        ?.mapNotNull { it.buildServiceAgent(participation) }
        ?.takeIf { it.isNotEmpty() }
}
