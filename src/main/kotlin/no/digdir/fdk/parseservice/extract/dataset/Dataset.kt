package no.digdir.fdk.parseservice.extract.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.containsTriple
import no.digdir.fdk.parseservice.extract.extractCatalogData
import no.digdir.fdk.parseservice.extract.extractHarvestMetaData
import no.digdir.fdk.parseservice.extract.fdkRecord
import no.digdir.fdk.parseservice.extract.primaryTopicFromFdkRecord
import no.digdir.fdk.parseservice.extract.singleObjectStatement
import no.digdir.fdk.parseservice.namespace.FDK
import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms

fun Model.extractDataset(): Dataset {
    val builder = Dataset()

    val record = fdkRecord(listOf(DCAT.Dataset))
    val dataset = record.primaryTopicFromFdkRecord(listOf(DCAT.Dataset))
    val harvestMetaData = record.extractHarvestMetaData()

    builder.id = record.singleObjectStatement(DCTerms.identifier)!!.string
    builder.uri = dataset.uri
    builder.harvest = harvestMetaData
    builder.catalog = dataset.extractCatalogData()

    builder.isRelatedToTransportportal = containsTriple(dataset.uri, FDK.isRelatedToTransportportal.uri, true)
    builder.isOpenData = containsTriple(dataset.uri, FDK.isOpenData.uri, true)
    builder.isAuthoritative = containsTriple(dataset.uri, FDK.isAuthoritative.uri, true)

    builder.addV11Values(dataset)

    return builder
}
