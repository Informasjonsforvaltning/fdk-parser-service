package no.digdir.fdk.parserservice.extract.informationmodel

import no.digdir.fdk.model.informationmodel.InformationModelStandard
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.isResource
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.OWL
import org.apache.jena.vocabulary.RDFS

private fun InformationModelStandard.hasContent(): Boolean =
    when {
        uri != null -> true
        title != null -> true
        seeAlso != null -> true
        versionInfo != null -> true
        else -> false
    }

/**
 * Extracts references to modelling standards/profiles and maps them to
 * `InformationModelStandard` objects containing title, seeAlso and version info.
 *
 * @param pred predicate (e.g. `prof:isProfileOf`) pointing to the standard resource
 * @return list of standards or `null` when no references exist
 */
fun Resource.extractListOfModelStandard(pred: Property): List<InformationModelStandard>? =
    listProperties(pred)
        .asSequence()
        .mapNotNull { it.buildModelStandard() }
        .toList()
        .takeIf { it.isNotEmpty() }

private fun Statement.buildModelStandard(): InformationModelStandard? {
    if (isResource(this)) {
        val builder = InformationModelStandard.newBuilder()

        return builder
            .setUri(resource.extractURIStringValue())
            .setTitle(resource.extractLocalizedStrings(DCTerms.title))
            .setSeeAlso(resource.extractListOfStrings(RDFS.seeAlso))
            .setVersionInfo(resource.extractStringValue(OWL.versionInfo))
            .build()
            .takeIf { it.hasContent() }
    } else {
        return null
    }
}
