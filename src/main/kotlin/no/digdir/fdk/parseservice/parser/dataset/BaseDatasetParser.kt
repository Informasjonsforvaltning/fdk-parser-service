package no.digdir.fdk.parseservice.parser.dataset

import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.descriptionHtmlCleaner
import no.digdir.fdk.parseservice.extract.extractCatalogData
import no.digdir.fdk.parseservice.extract.extractEuDataTheme
import no.digdir.fdk.parseservice.extract.extractEurovoc
import no.digdir.fdk.parseservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parseservice.extract.extractListOfStrings
import no.digdir.fdk.parseservice.extract.dataset.extractListOfSubjects
import no.digdir.fdk.parseservice.extract.extractListOfUriWithLabel
import no.digdir.fdk.parseservice.extract.extractLocalizedStringList
import no.digdir.fdk.parseservice.extract.extractLocalizedStrings
import no.digdir.fdk.parseservice.extract.extractLosNode
import no.digdir.fdk.parseservice.extract.extractOrganization
import no.digdir.fdk.parseservice.extract.extractReferenceDataCode
import no.digdir.fdk.parseservice.extract.extractStringValue
import no.digdir.fdk.parseservice.extract.isEuDataThemeURI
import no.digdir.fdk.parseservice.extract.isEurovocURI
import no.digdir.fdk.parseservice.extract.isLosURI
import no.digdir.fdk.parseservice.extract.listResources
import no.digdir.fdk.parseservice.vocabulary.ADMS
import no.digdir.fdk.parseservice.vocabulary.EUAT
import no.digdir.fdk.parseservice.parser.DatasetParserStrategy
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.SKOS

/**
 * Abstract base class for DCAT-AP dataset parsers.
 * 
 * This class provides common functionality for parsing DCAT-AP datasets
 * and serves as a foundation for version-specific implementations.
 * It handles the extraction of common dataset properties that are shared
 * across different DCAT-AP versions.
 * 
 * Subclasses must implement the abstract methods to provide version-specific
 * configuration and parsing logic.
 * 
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 * @see DatasetParserStrategy
 * @see DcatApNoV1Parser
 */
abstract class BaseDatasetParser : DatasetParserStrategy {
    /**
     * Parses an RDF model into a Dataset object.
     * 
     * This method must be implemented by subclasses to provide
     * version-specific parsing logic.
     * 
     * @param model The Jena RDF model containing the dataset
     * @param iri The IRI of the dataset
     * @return The parsed Dataset object
     * @throws IllegalArgumentException if the model is null or invalid
     * @throws UnsupportedOperationException if the model format is not supported
     */
    abstract override fun parse(model: Model, iri: String): Dataset

    /**
     * Gets the default language for this parser version.
     * 
     * @return The default language code (e.g., "no", "en")
     */
    protected abstract fun getDefaultLanguage(): String

    /**
     * Gets the version string for this parser.
     * 
     * @return The version string (e.g., "1.1")
     */
    protected abstract fun getVersion(): String

    /**
     * Gets the source format identifier for this parser.
     * 
     * @return The source format (e.g., "DCAT-AP-NO")
     */
    protected abstract fun getSourceFormat(): String

    /**
     * Gets a list of acceptable RDF types for harvested datasets.
     * 
     * @return List of acceptable RDF types (e.g., [DCAT.Dataset])
     */
    protected abstract fun getAcceptableTypes(): List<Resource>

    /**
     * Adds common dataset values to the builder that are shared across all versions.
     * 
     * This method extracts and sets common properties such as title, description,
     * publisher, themes, and other metadata that are consistent across DCAT-AP versions.
     * 
     * @param datasetResource The RDF resource representing the dataset
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
        setAccessRights(datasetResource.extractReferenceDataCode(DCTerms.accessRights, DC_11.identifier, SKOS.prefLabel))
        setLanguage(datasetResource.extractListOfReferenceDataCodes(DCTerms.language, EUAT.authorityCode, SKOS.prefLabel))
        setKeyword(datasetResource.extractLocalizedStringList(DCAT.keyword))

        val themeResources = datasetResource.listResources(DCAT.theme)
            ?.filter { it.isURIResource }
            ?.takeIf { it.isNotEmpty() }

        setThemeUris(themeResources?.map { it.uri })
        setTheme(themeResources?.filter { isEuDataThemeURI(it.uri) }?.map { it.extractEuDataTheme() })
        setLosTheme(themeResources?.filter { isLosURI(it.uri) }?.map { it.extractLosNode() })
        setEurovocThemes(themeResources?.filter { isEurovocURI(it.uri) }?.map { it.extractEurovoc() })

        setAccrualPeriodicity(datasetResource.extractReferenceDataCode(DCTerms.accrualPeriodicity, DC_11.identifier, SKOS.prefLabel))
        setDctType(datasetResource.extractReferenceDataCode(DCTerms.type, DC_11.identifier, SKOS.prefLabel))
        setProvenance(datasetResource.extractReferenceDataCode(DCTerms.provenance, EUAT.authorityCode, SKOS.prefLabel))
        setSpatial(datasetResource.extractListOfReferenceDataCodes(DCTerms.spatial, DCTerms.identifier, DCTerms.title))
        setConformsTo(datasetResource.extractListOfUriWithLabel(DCTerms.conformsTo, DCTerms.source, DCTerms.title))
        setSubject(datasetResource.extractListOfSubjects())

        setType(ResourceType.datasets)
    }
}
