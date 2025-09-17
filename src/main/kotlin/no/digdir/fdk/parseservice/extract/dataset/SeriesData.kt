package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.dataset.InSeries
import no.digdir.fdk.parseservice.extract.containsTriple
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.extract.extractURIStringValue
import no.digdir.fdk.parseservice.extract.singleResource
import no.digdir.fdk.parseservice.vocabulary.DCAT3
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import java.net.URI

fun Resource.extractInSeries(): InSeries? {
    val builder = InSeries.newBuilder()
    val seriesResource = singleResource(DCAT3.inSeries)

    if (seriesResource?.isURIResource == true) {
        val seriesCatalogRecord = model.listResourcesWithProperty(RDF.type, DCAT.CatalogRecord)
            .asSequence()
            .filter { it.isURIResource }
            .filter { model.containsTriple(it.uri, FOAF.primaryTopic.uri, URI.create(seriesResource.uri)) }
            .toList()
            .firstOrNull()

        builder.setUri(seriesResource.extractURIStringValue())
            .setId(seriesCatalogRecord?.extractStringValue(DCTerms.identifier))
            .setTitle(seriesResource.extractLocalizedStrings(DCTerms.title))

        return builder.build()
    } else {
        return null
    }
}

fun Resource.extractListOfDatasetsInSeries() : List<String>? {
    var current = singleResource(DCAT3.last)
    val datasets = mutableListOf<String>()

    while (current?.isURIResource == true && !datasets.contains(current.uri)) {
        datasets.add(current.uri)
        current = current.singleResource(DCAT3.prev)
    }

    return datasets.takeIf { it.isNotEmpty() }
}
