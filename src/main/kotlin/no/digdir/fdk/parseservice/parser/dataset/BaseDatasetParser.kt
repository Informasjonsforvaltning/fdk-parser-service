package no.digdir.fdk.parseservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.containsTriple
import no.digdir.fdk.parseservice.extract.extractCatalogData
import no.digdir.fdk.parseservice.extract.singleObjectStatement
import no.digdir.fdk.parseservice.namespace.FDK
import no.digdir.fdk.parseservice.parser.DatasetParserStrategy
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms

/**
 * Base parser for DCAT-AP dataset parsers
 */
abstract class BaseDatasetParser : DatasetParserStrategy {
    /**
     * Abstract parse method that must be implemented by subclasses
     */
    abstract override fun parse(model: Model): Dataset

    /**
     * Get the default language for this parser version
     */
    protected abstract fun getDefaultLanguage(): String

    /**
     * Get the version string for this parser
     */
    protected abstract fun getVersion(): String

    /**
     * Get the source format for this parser
     */
    protected abstract fun getSourceFormat(): String

    /**
     * Get the URI pattern used by relevant FDK records with harvest metadata
     */
    protected abstract fun getFDKURIPattern(): String

    /**
     * Get a list of acceptable RDF types of harvested datasets, i.e. DCAT:Dataset
     */
    protected abstract fun getAcceptableTypes(): List<Resource>

    /**
     * Get a builder for Dataset with common fields already added
     */
    protected fun getDatasetBuilder(recordResource: Resource, datasetResource: Resource): Dataset.Builder {
        val builder = Dataset.newBuilder()

        builder.id = recordResource.singleObjectStatement(DCTerms.identifier)!!.string
        builder.uri = datasetResource.uri
        builder.harvest = harvestMetaData(recordResource)
        builder.catalog = datasetResource.extractCatalogData()

        builder.isRelatedToTransportportal = datasetResource.model.containsTriple(datasetResource.uri, FDK.isRelatedToTransportportal.uri, true)
        builder.isOpenData = datasetResource.model.containsTriple(datasetResource.uri, FDK.isOpenData.uri, true)
        builder.isAuthoritative = datasetResource.model.containsTriple(datasetResource.uri, FDK.isAuthoritative.uri, true)

        return builder
    }
}
