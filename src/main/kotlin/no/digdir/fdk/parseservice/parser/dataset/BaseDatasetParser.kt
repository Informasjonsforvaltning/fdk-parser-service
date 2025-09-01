package no.digdir.fdk.parseservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.descriptionHtmlCleaner
import no.digdir.fdk.parseservice.extract.extractCatalogData
import no.digdir.fdk.parseservice.extract.extractListOfStrings
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.extractOrganization
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.namespace.ADMS
import no.digdir.fdk.parseservice.parser.DatasetParserStrategy
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
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
     * Add data to the builder that is common among all dataset versions
     */
    protected fun Dataset.Builder.addCommonDatasetValues(datasetResource: Resource) {
        val formattedDescription = datasetResource.extractLocalizedStrings(DCTerms.description)

        setUri(datasetResource.uri)
        setCatalog(datasetResource.extractCatalogData())

        setTitle(datasetResource.extractLocalizedStrings(DCTerms.title))
        setDescriptionFormatted(formattedDescription)
        setDescription(formattedDescription?.descriptionHtmlCleaner())
        setPublisher(datasetResource.extractOrganization(DCTerms.publisher))
        setIdentifier(datasetResource.extractListOfStrings(DCTerms.identifier))
        setAdmsIdentifier(datasetResource.extractListOfStrings(ADMS.identifier))
        setModified(datasetResource.extractStringValue(DCTerms.modified))
        setIssued(datasetResource.extractStringValue(DCTerms.issued))
        setLandingPage(datasetResource.extractListOfStrings(DCAT.landingPage))
        setPage(datasetResource.extractListOfStrings(FOAF.page))
    }
}
