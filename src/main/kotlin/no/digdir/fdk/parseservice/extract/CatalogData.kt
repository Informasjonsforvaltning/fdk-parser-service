package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.Catalog
import no.digdir.fdk.parseservice.LOGGER
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import java.net.URI

private fun Catalog.hasData(): Boolean =
    uri != null || id != null || title != null || description != null || publisher != null

fun Resource.extractCatalogData(): Catalog? {
    val catalogResource = getCatalogResource()

    if (catalogResource == null) return null
    else {
        val builder = Catalog.newBuilder()

        val catalog = builder.setUri(catalogResource.extractURIStringValue())
            .setId(catalogResource.extractStringValue(DCTerms.identifier))
            .setTitle(catalogResource.extractLocalizedStrings(DCTerms.title))
            .setDescription(catalogResource.extractLocalizedStrings(DCTerms.description))
            .setPublisher(catalogResource.extractOrganization(DCTerms.publisher))
            .build()

        return if (catalog.hasData()) catalog else null
    }
}

private fun Resource.getCatalogResource(): Resource? {
    val catalogs = model.listSubjectsWithProperty(RDF.type, DCAT.Catalog)
        .asSequence()
        .filter { it.isURIResource }
        .filter { model.containsTriple(it.uri, DCAT.dataset.uri, URI.create(uri)) }
        .toList()

    return when {
        catalogs.isEmpty() -> null
        catalogs.size == 1 -> catalogs.first()
        else -> {
            LOGGER.warn("Expecting 1 catalog with $uri as a member, found ${catalogs.size}. Selecting random as backup")
            catalogs.first()
        }
    }
}
