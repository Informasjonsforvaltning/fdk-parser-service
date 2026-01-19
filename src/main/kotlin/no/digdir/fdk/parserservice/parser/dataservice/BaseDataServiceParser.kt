package no.digdir.fdk.parserservice.parser.dataservice

import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.dataservice.DataService
import no.digdir.fdk.parserservice.extract.descriptionHtmlCleaner
import no.digdir.fdk.parserservice.extract.extractCatalogData
import no.digdir.fdk.parserservice.extract.extractEuDataTheme
import no.digdir.fdk.parserservice.extract.extractEurovoc
import no.digdir.fdk.parserservice.extract.extractListOfContactPoints
import no.digdir.fdk.parserservice.extract.extractListOfFormats
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractListOfUriWithLabel
import no.digdir.fdk.parserservice.extract.extractLocalizedStringList
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractLosNode
import no.digdir.fdk.parserservice.extract.extractOrganization
import no.digdir.fdk.parserservice.extract.extractReferenceDataCode
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.isEuDataThemeURI
import no.digdir.fdk.parserservice.extract.isEurovocURI
import no.digdir.fdk.parserservice.extract.isLosURI
import no.digdir.fdk.parserservice.extract.isSkolemizedURI
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.parser.DataServiceParserStrategy
import no.digdir.fdk.parserservice.vocabulary.EUAT
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.SKOS

/**
 * Abstract base class for DCAT-AP data service parsers.
 *
 * This class provides common functionality for parsing DCAT-AP data services
 * and serves as a foundation for version-specific implementations.
 * It handles the extraction of common data service properties that are shared
 * across different DCAT-AP versions.
 *
 * Subclasses must implement the abstract methods to provide version-specific
 * configuration and parsing logic.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 * @see DataServiceParserStrategy
 */
abstract class BaseDataServiceParser : DataServiceParserStrategy {
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
     * Gets a list of acceptable RDF types for harvested data services.
     *
     * @return List of acceptable RDF types (e.g., [DCAT.DataService])
     */
    protected abstract fun getAcceptableTypes(): List<Resource>

    /**
     * Adds common data service values to the builder that are shared across all versions.
     *
     * This method extracts and sets common properties such as title, description,
     * publisher, themes, and other metadata that are consistent across DCAT-AP versions.
     *
     * @param dataServiceResource The RDF resource representing the data service
     */
    protected fun DataService.Builder.addCommonDataServiceValues(dataServiceResource: Resource) {
        val formattedDescription = dataServiceResource.extractLocalizedStrings(DCTerms.description)

        setUri(dataServiceResource.uri)
        setCatalog(dataServiceResource.extractCatalogData(DCAT.service))

        setTitle(dataServiceResource.extractLocalizedStrings(DCTerms.title))
        setDescriptionFormatted(formattedDescription)
        setDescription(formattedDescription?.descriptionHtmlCleaner())

        // The multiplicity of endpointURL is changed from 1..n to 1..1 in DCAT-AP-NO vs. DCAT-AP
        setEndpointURL(dataServiceResource.extractStringValue(DCAT.endpointURL)?.let { listOf(it) })

        setEndpointDescription(dataServiceResource.extractListOfStrings(DCAT.endpointDescription))
        setServesDataset(dataServiceResource.extractListOfStrings(DCAT.servesDataset))

        setPublisher(dataServiceResource.extractOrganization(DCTerms.publisher))
        setIdentifier(dataServiceResource.extractListOfStrings(DCTerms.identifier))
        setModified(dataServiceResource.extractStringValue(DCTerms.modified))
        setIssued(dataServiceResource.extractStringValue(DCTerms.issued))
        setLandingPage(dataServiceResource.extractListOfStrings(DCAT.landingPage))
        setPage(dataServiceResource.extractListOfStrings(FOAF.page))
        setAccessRights(dataServiceResource.extractReferenceDataCode(DCTerms.accessRights, DC_11.identifier, SKOS.prefLabel))
        setLanguage(dataServiceResource.extractListOfReferenceDataCodes(DCTerms.language, EUAT.authorityCode, SKOS.prefLabel))
        setKeyword(dataServiceResource.extractLocalizedStringList(DCAT.keyword))

        setDctType(dataServiceResource.extractStringValue(DCTerms.type))
        setConformsTo(dataServiceResource.extractListOfUriWithLabel(DCTerms.conformsTo, DCTerms.source, DCTerms.title))
        setContactPoint(dataServiceResource.extractListOfContactPoints())

        val formats = dataServiceResource.extractListOfFormats(DCTerms.format) ?: emptyList()
        val mediaTypes = dataServiceResource.extractListOfFormats(DCAT.mediaType) ?: emptyList()
        val allFormats = formats + mediaTypes
        setFdkFormat(allFormats.takeIf { it.isNotEmpty() })

        val themeResources =
            dataServiceResource
                .listResources(DCAT.theme)
                ?.filter { it.isURIResource && !isSkolemizedURI(it.uri) }
                ?.takeIf { it.isNotEmpty() }

        setThemeUris(themeResources?.map { it.uri })
        setTheme(themeResources?.filter { isEuDataThemeURI(it.uri) }?.mapNotNull { it.extractEuDataTheme() }?.takeIf { it.isNotEmpty() })
        setLosTheme(themeResources?.filter { isLosURI(it.uri) }?.mapNotNull { it.extractLosNode() }?.takeIf { it.isNotEmpty() })
        setEurovocThemes(themeResources?.filter { isEurovocURI(it.uri) }?.mapNotNull { it.extractEurovoc() }?.takeIf { it.isNotEmpty() })

        setType(ResourceType.dataservices)
    }
}
