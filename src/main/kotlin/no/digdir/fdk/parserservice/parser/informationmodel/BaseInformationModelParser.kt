package no.digdir.fdk.parserservice.parser.informationmodel

import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.informationmodel.InformationModel
import no.digdir.fdk.parserservice.extract.*
import no.digdir.fdk.parserservice.extract.informationmodel.buildModelElement
import no.digdir.fdk.parserservice.extract.informationmodel.buildModelProperty
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

        val themeResources = infoModelResource.listResources(DCAT.theme)
            ?.filter { it.isURIResource && !isSkolemizedURI(it.uri) }
            ?.takeIf { it.isNotEmpty() }

        setThemeUris(themeResources?.map { it.uri })
        setTheme(themeResources?.filter { isEuDataThemeURI(it.uri) }?.map { it.extractEuDataTheme() })
        setLosTheme(themeResources?.filter { isLosURI(it.uri) }?.map { it.extractLosNode() })
        setEurovocThemes(themeResources?.filter { isEurovocURI(it.uri) }?.map { it.extractEurovoc() })

        setDctType(infoModelResource.extractStringValue(DCTerms.type))
        setConformsTo(infoModelResource.extractListOfUriWithLabel(DCTerms.conformsTo, DCTerms.source, DCTerms.title))
        setLicense(infoModelResource.extractListOfReferenceDataCodes(DCTerms.license, DC_11.identifier, SKOS.prefLabel))
        setInformationModelIdentifier(infoModelResource.extractStringValue(MODELLDCATNO.informationModelIdentifier))
        setSpatial(infoModelResource.extractListOfReferenceDataCodes(DCTerms.spatial, DCTerms.identifier, DCTerms.title))
        setTemporal(infoModelResource.extractListOfTemporal(DCTerms.temporal, DCAT.startDate, DCAT.endDate))
        setIsPartOf(infoModelResource.extractStringValue(DCTerms.isPartOf))
        setHasPart(infoModelResource.extractStringValue(DCTerms.hasPart))
        setIsReplacedBy(infoModelResource.extractStringValue(DCTerms.isReplacedBy))
        setIsProfileOf(infoModelResource.extractListOfUriWithLabel(PROF.isProfileOf, DCTerms.source, DCTerms.title))
        setReplaces(infoModelResource.extractStringValue(DCTerms.replaces))
        setHasFormat(infoModelResource.extractListOfFormats(DCTerms.hasFormat))
        setHomepage(infoModelResource.extractStringValue(FOAF.homepage))
        setStatus(infoModelResource.extractStringValue(ADMS.status))
        setVersionInfo(infoModelResource.extractStringValue(OWL.versionInfo))
        setVersionNotes(infoModelResource.extractLocalizedStrings(ADMS.versionNotes))
        setSubjects(infoModelResource.extractListOfStrings(DCTerms.subject))
        setContainsSubjects(infoModelResource.extractListOfStrings(DCTerms.subject))

        // Extract model elements and properties
        val modelElementsMap = mutableMapOf<CharSequence, no.digdir.fdk.model.informationmodel.ModelElement>()
        val modelPropertiesMap = mutableMapOf<CharSequence, no.digdir.fdk.model.informationmodel.ModelProperty>()
        val containsModelElementsList = mutableListOf<CharSequence>()

        infoModelResource.listResources(MODELLDCATNO.containsModelElement)?.forEach { elementResource ->
            val element = elementResource.buildModelElement()
            if (element != null) {
                val elementKey = element.uri ?: element.identifier
                if (elementKey != null) {
                    modelElementsMap[elementKey] = element
                    containsModelElementsList.add(elementKey)

                    // Extract properties from elements
                    elementResource.listResources(MODELLDCATNO.hasProperty)?.forEach { propertyResource ->
                        val property = propertyResource.buildModelProperty()
                        if (property != null) {
                            val propertyKey = property.uri ?: property.identifier
                            if (propertyKey != null) {
                                modelPropertiesMap[propertyKey] = property
                            }
                        }
                    }
                }
            }
        }

        setModelElements(modelElementsMap.takeIf { it.isNotEmpty() })
        setModelProperties(modelPropertiesMap.takeIf { it.isNotEmpty() })
        setContainsModelElements(containsModelElementsList.takeIf { it.isNotEmpty() })

        setType(ResourceType.informationmodels)
    }
}
