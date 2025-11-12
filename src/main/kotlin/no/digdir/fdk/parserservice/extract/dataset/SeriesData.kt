package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.dataset.InSeries
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.singleResource
import no.digdir.fdk.parserservice.vocabulary.DCAT3
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import java.net.URI

/**
 * Extracts the dataset series metadata by resolving the `dcat3:inSeries` reference and
 * mapping relevant catalog record data.
 *
 * @return `InSeries` descriptor or `null` when the resource is not part of a series
 */
fun Resource.extractInSeries(): InSeries? {
    val builder = InSeries.newBuilder()
    val seriesResource = singleResource(DCAT3.inSeries)

    if (seriesResource?.isURIResource == true) {
        val seriesCatalogRecord =
            model
                .listResourcesWithProperty(RDF.type, DCAT.CatalogRecord)
                .asSequence()
                .filter { it.isURIResource }
                .filter { model.containsTriple(it.uri, FOAF.primaryTopic.uri, URI.create(seriesResource.uri)) }
                .toList()
                .firstOrNull()

        builder
            .setUri(seriesResource.extractURIStringValue())
            .setId(seriesCatalogRecord?.extractStringValue(DCTerms.identifier))
            .setTitle(seriesResource.extractLocalizedStrings(DCTerms.title))

        return builder.build()
    } else {
        return null
    }
}

/**
 * Traverses the linked-list structure defined by `dcat3:last`/`dcat3:prev` to collect
 * the URIs of datasets that belong to the same series.
 *
 * @return ordered list of dataset URIs or `null` when the linkage is absent
 */
fun Resource.extractListOfDatasetsInSeries(): List<String>? {
    var current = singleResource(DCAT3.last)
    val datasets = mutableListOf<String>()

    while (current?.isURIResource == true && !datasets.contains(current.uri)) {
        datasets.add(current.uri)
        current = current.singleResource(DCAT3.prev)
    }

    return datasets.takeIf { it.isNotEmpty() }
}
