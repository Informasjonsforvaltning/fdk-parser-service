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
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms

fun Model.extractDataset(): Dataset {
    val builderV1 = Dataset.newBuilder()
    //val builderV2 = Dataset.newBuilder()
    //val builderV3 = Dataset.newBuilder()

    val recordResource = fdkRecord(listOf(DCAT.Dataset))
    val datasetResource = recordResource.primaryTopicFromFdkRecord(listOf(DCAT.Dataset))

    addCommonDatasetValuesToBuilder(recordResource, datasetResource, builderV1)
    //addCommonDatasetValuesToBuilder(recordResource, datasetResource, builderV2)
    //addCommonDatasetValuesToBuilder(recordResource, datasetResource, builderV3)

    datasetResource.addV11ValuesToBuilder(builderV1)
    //datasetResource.addV2ValuesToBuilder(builderV2)
    //datasetResource.addV3ValuesToBuilder(builderV3)

    // return combineDatasets(builderV1.build(), builderV2.build(), builderV3.build())
    return builderV1.build()
}

private fun Model.addCommonDatasetValuesToBuilder(
    recordResource: Resource,
    datasetResource: Resource,
    builder: Dataset.Builder
) {
    val harvestMetaData = recordResource.extractHarvestMetaData()

    builder.id = recordResource.singleObjectStatement(DCTerms.identifier)!!.string
    builder.uri = datasetResource.uri
    builder.harvest = harvestMetaData
    builder.catalog = datasetResource.extractCatalogData()

    builder.isRelatedToTransportportal = containsTriple(datasetResource.uri, FDK.isRelatedToTransportportal.uri, true)
    builder.isOpenData = containsTriple(datasetResource.uri, FDK.isOpenData.uri, true)
    builder.isAuthoritative = containsTriple(datasetResource.uri, FDK.isAuthoritative.uri, true)
}
