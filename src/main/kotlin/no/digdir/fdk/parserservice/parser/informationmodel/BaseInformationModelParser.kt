package no.digdir.fdk.parserservice.parser.informationmodel

import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.informationmodel.InformationModel
import no.digdir.fdk.parserservice.extract.descriptionHtmlCleaner
import no.digdir.fdk.parserservice.extract.extractCatalogData
import no.digdir.fdk.parserservice.extract.extractEuDataTheme
import no.digdir.fdk.parserservice.extract.extractEurovoc
import no.digdir.fdk.parserservice.extract.extractListOfContactPoints
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractListOfTemporal
import no.digdir.fdk.parserservice.extract.extractLocalizedStringList
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractLosNode
import no.digdir.fdk.parserservice.extract.extractOrganization
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.informationmodel.extractListOfModelFormat
import no.digdir.fdk.parserservice.extract.informationmodel.extractListOfModelStandard
import no.digdir.fdk.parserservice.extract.isEuDataThemeURI
import no.digdir.fdk.parserservice.extract.isEurovocURI
import no.digdir.fdk.parserservice.extract.isLosURI
import no.digdir.fdk.parserservice.extract.isSkolemizedURI
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.parser.InformationModelParserStrategy
import no.digdir.fdk.parserservice.vocabulary.ADMS
import no.digdir.fdk.parserservice.vocabulary.EUAT
import no.digdir.fdk.parserservice.vocabulary.MODELLDCATNO
import no.digdir.fdk.parserservice.vocabulary.PROF
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.OWL
import org.apache.jena.vocabulary.SKOS

/**
 * Abstract base class for ModellDCAT-AP-NO information model parsers.
 *
 * This class provides common functionality for parsing ModellDCAT-AP-NO information models
 * and serves as a foundation for version-specific implementations.
 * It handles the extraction of common information model properties that are shared
 * across different ModelDCAT-AP-NO versions.
 *
 * Subclasses must implement the abstract methods to provide version-specific
 * configuration and parsing logic.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 * @see InformationModelParserStrategy
 */
abstract class BaseInformationModelParser : InformationModelParserStrategy {
    /**
     * Gets the default language for this parser version.
     *
     * @return The default language code (e.g., "no", "en")
     */
    protected abstract fun getDefaultLanguage(): String

    /**
     * Gets the version string for this parser.
     *
     * @return The version string (e.g., "1.0")
     */
    protected abstract fun getVersion(): String

    /**
     * Gets the source format identifier for this parser.
     *
     * @return The source format (e.g., "ModellDCAT-AP-NO")
     */
    protected abstract fun getSourceFormat(): String

    /**
     * Gets a list of acceptable RDF types for harvested information models.
     *
     * @return List of acceptable RDF types (e.g., [MODELLDCATNO.InformationModel])
     */
    protected abstract fun getAcceptableTypes(): List<Resource>

    /**
     * Adds common information model values to the builder that are shared across all versions.
     *
     * This method extracts and sets common properties such as title, description,
     * publisher, themes, and other metadata that are consistent across ModelDCAT-AP-NO versions.
     *
     * @param infoModelResource The RDF resource representing the information model
     */
    protected fun InformationModel.Builder.addCommonInformationModelValues(infoModelResource: Resource) {
        val formattedDescription = infoModelResource.extractLocalizedStrings(DCTerms.description)

        setUri(infoModelResource.uri)
        setCatalog(infoModelResource.extractCatalogData())

        setTitle(infoModelResource.extractLocalizedStrings(DCTerms.title))
        setDescriptionFormatted(formattedDescription)
        setDescription(formattedDescription?.descriptionHtmlCleaner())
        setPublisher(infoModelResource.extractOrganization(DCTerms.publisher))
        setIdentifier(infoModelResource.extractListOfStrings(DCTerms.identifier))
        setModified(infoModelResource.extractStringValue(DCTerms.modified))
        setIssued(infoModelResource.extractStringValue(DCTerms.issued))
        setLanguage(infoModelResource.extractListOfReferenceDataCodes(DCTerms.language, EUAT.authorityCode, SKOS.prefLabel))
        setKeyword(infoModelResource.extractLocalizedStringList(DCAT.keyword))

        setContactPoint(infoModelResource.extractListOfContactPoints())

        val themeResources =
            infoModelResource
                .listResources(DCAT.theme)
                ?.filter { it.isURIResource && !isSkolemizedURI(it.uri) }
                ?.takeIf { it.isNotEmpty() }

        setThemeUris(themeResources?.map { it.uri })
        setTheme(themeResources?.filter { isEuDataThemeURI(it.uri) }?.mapNotNull { it.extractEuDataTheme() }?.takeIf { it.isNotEmpty() })
        setLosTheme(themeResources?.filter { isLosURI(it.uri) }?.mapNotNull { it.extractLosNode() }?.takeIf { it.isNotEmpty() })
        setEurovocThemes(themeResources?.filter { isEurovocURI(it.uri) }?.mapNotNull { it.extractEurovoc() }?.takeIf { it.isNotEmpty() })

        setDctType(infoModelResource.extractStringValue(DCTerms.type))
        setConformsTo(infoModelResource.extractListOfModelStandard(DCTerms.conformsTo))
        setLicense(infoModelResource.extractListOfReferenceDataCodes(DCTerms.license, DC_11.identifier, SKOS.prefLabel))
        setInformationModelIdentifier(infoModelResource.extractStringValue(MODELLDCATNO.informationModelIdentifier))
        setSpatial(infoModelResource.extractListOfReferenceDataCodes(DCTerms.spatial, DCTerms.identifier, DCTerms.title))
        setTemporal(infoModelResource.extractListOfTemporal(DCTerms.temporal, DCAT.startDate, DCAT.endDate))
        setIsPartOf(infoModelResource.extractStringValue(DCTerms.isPartOf))
        setHasPart(infoModelResource.extractStringValue(DCTerms.hasPart))
        setIsReplacedBy(infoModelResource.extractStringValue(DCTerms.isReplacedBy))
        setIsProfileOf(infoModelResource.extractListOfModelStandard(PROF.isProfileOf))
        setReplaces(infoModelResource.extractStringValue(DCTerms.replaces))
        setHasFormat(infoModelResource.extractListOfModelFormat())
        setHomepage(infoModelResource.extractStringValue(FOAF.homepage))
        setStatus(infoModelResource.extractStringValue(ADMS.status))
        setVersionInfo(infoModelResource.extractStringValue(OWL.versionInfo))
        setVersionNotes(infoModelResource.extractLocalizedStrings(ADMS.versionNotes))

        setType(ResourceType.informationmodels)
    }
}
