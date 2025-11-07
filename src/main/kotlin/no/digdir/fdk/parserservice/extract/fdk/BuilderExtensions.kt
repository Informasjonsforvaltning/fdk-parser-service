package no.digdir.fdk.parserservice.extract.fdk

import no.digdir.fdk.model.dataservice.DataService
import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.vocabulary.FDK
import org.apache.jena.rdf.model.Resource

/**
 * Adds data to the dataset builder that has been added to the dataset graph by the FDK harvest process.
 * @param recordResource The jena resource of the fdkRecord
 * @param datasetResource The jena resource of the dataset
 */
fun Dataset.Builder.addFdkData(
    recordResource: Resource,
    datasetResource: Resource,
) {
    setId(fdkIdFromRecord(recordResource))
    setHarvest(harvestMetaData(recordResource))

    setIsRelatedToTransportportal(datasetResource.model.containsTriple(datasetResource.uri, FDK.isRelatedToTransportportal.uri, true))
    setIsOpenData(datasetResource.model.containsTriple(datasetResource.uri, FDK.isOpenData.uri, true))
    setIsAuthoritative(datasetResource.model.containsTriple(datasetResource.uri, FDK.isAuthoritative.uri, true))
}

/**
 * Adds data to the data service builder that has been added to the data service graph by the FDK harvest process.
 * @param recordResource The jena resource of the fdkRecord
 */
fun DataService.Builder.addFdkData(recordResource: Resource) {
    setId(fdkIdFromRecord(recordResource))
    setHarvest(harvestMetaData(recordResource))
}
