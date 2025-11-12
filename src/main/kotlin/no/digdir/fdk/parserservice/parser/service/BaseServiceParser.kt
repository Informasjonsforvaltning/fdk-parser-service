package no.digdir.fdk.parserservice.parser.service

import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.service.Service
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.descriptionHtmlCleaner
import no.digdir.fdk.parserservice.extract.extractCatalogData
import no.digdir.fdk.parserservice.extract.extractEuDataTheme
import no.digdir.fdk.parserservice.extract.extractEurovoc
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractListOfUriWithLabel
import no.digdir.fdk.parserservice.extract.extractLocalizedStringList
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractLosNode
import no.digdir.fdk.parserservice.extract.extractReferenceDataCode
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.isEuDataThemeURI
import no.digdir.fdk.parserservice.extract.isEurovocURI
import no.digdir.fdk.parserservice.extract.isLosURI
import no.digdir.fdk.parserservice.extract.isSkolemizedURI
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.parser.ServiceParserStrategy
import no.digdir.fdk.parserservice.vocabulary.CPSV
import no.digdir.fdk.parserservice.vocabulary.CV
import no.digdir.fdk.parserservice.vocabulary.EUAT
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS
import java.net.URI

/**
 * Abstract base class for service parsers.
 *
 * This class provides common functionality for parsing services
 * and serves as a foundation for version-specific implementations.
 * It handles the extraction of common service properties that are shared
 * across different versions.
 *
 * Subclasses must implement the abstract methods to provide version-specific
 * configuration and parsing logic.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 * @see ServiceParserStrategy
 */
abstract class BaseServiceParser : ServiceParserStrategy {
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
     * @return The source format (e.g., "CPSV-AP-NO")
     */
    protected abstract fun getSourceFormat(): String

    /**
     * Gets a list of acceptable RDF types for harvested services.
     *
     * @return List of acceptable RDF types (e.g., [CPSVNO.Service, CPSV.PublicService])
     */
    protected abstract fun getAcceptableTypes(): List<Resource>

    /**
     * Adds common service values to the builder that are shared across all versions.
     *
     * This method extracts and sets common properties such as title, description,
     * publisher, themes, and other metadata that are consistent across versions.
     *
     * @param serviceResource The RDF resource representing the service
     */
    protected fun Service.Builder.addCommonServiceValues(serviceResource: Resource) {
        val formattedDescription = serviceResource.extractLocalizedStrings(DCTerms.description)

        setUri(serviceResource.uri)
        setCatalog(serviceResource.extractCatalogData())

        setTitle(serviceResource.extractLocalizedStrings(DCTerms.title))
        setDescription(formattedDescription?.descriptionHtmlCleaner())

        setIdentifier(serviceResource.extractStringValue(DCTerms.identifier))
        setHomepage(serviceResource.extractListOfStrings(FOAF.homepage))
        setLanguage(serviceResource.extractListOfReferenceDataCodes(DCTerms.language, EUAT.authorityCode, SKOS.prefLabel))
        setKeyword(serviceResource.extractLocalizedStringList(DCAT.keyword))
        setSpatial(serviceResource.extractListOfStrings(DCTerms.spatial))
        setProcessingTime(serviceResource.extractStringValue(CV.processingTime))
        setAdmsStatus(serviceResource.extractReferenceDataCode(DCTerms.type, DC_11.identifier, SKOS.prefLabel))

        setHasParticipation(serviceResource.extractListOfStrings(CV.hasParticipation))
        setIsGroupedBy(serviceResource.extractListOfStrings(CV.isGroupedBy))

        setRequires(serviceResource.extractListOfUriWithLabel(DCTerms.requires, DCTerms.title))
        setRelation(serviceResource.extractListOfUriWithLabel(DCTerms.relation, DCTerms.title))
        setDctType(serviceResource.extractListOfReferenceDataCodes(DCTerms.type, DC_11.identifier, SKOS.prefLabel))
        setSubject(serviceResource.extractListOfUriWithLabel(DCTerms.subject, DCTerms.source, SKOS.prefLabel))

        val themeResources =
            serviceResource
                .listResources(DCAT.theme)
                ?.filter { it.isURIResource && !isSkolemizedURI(it.uri) }
                ?.takeIf { it.isNotEmpty() }

        setThematicAreaUris(themeResources?.map { it.uri })
        setEuDataThemes(
            themeResources?.filter { isEuDataThemeURI(it.uri) }?.mapNotNull { it.extractEuDataTheme() }?.takeIf { it.isNotEmpty() },
        )
        setLosThemes(themeResources?.filter { isLosURI(it.uri) }?.mapNotNull { it.extractLosNode() }?.takeIf { it.isNotEmpty() })
        setEurovocThemes(themeResources?.filter { isEurovocURI(it.uri) }?.mapNotNull { it.extractEurovoc() }?.takeIf { it.isNotEmpty() })

        setSector(serviceResource.extractListOfUriWithLabel(CV.sector, DCTerms.source, SKOS.prefLabel))
        setIsClassifiedBy(serviceResource.extractListOfUriWithLabel(CV.isClassifiedBy, DCTerms.source, SKOS.prefLabel))
        setIsDescribedAt(serviceResource.extractListOfUriWithLabel(CV.isDescribedAt, DCTerms.title))

        val isPublicService = serviceResource.model.containsTriple(serviceResource.uri, RDF.type.uri, URI.create(CPSV.PublicService.uri))
        if (isPublicService) {
            val competentAuthorities =
                serviceResource
                    .listResources(no.digdir.fdk.parserservice.vocabulary.CV.hasCompetentAuthority)
                    ?.mapNotNull { orgResource ->
                        val orgBuilder =
                            no.digdir.fdk.model.Organization
                                .newBuilder()
                        orgBuilder
                            .setUri(orgResource.extractURIStringValue())
                            .setId(orgResource.extractStringValue(org.apache.jena.vocabulary.DCTerms.identifier))
                            .setName(orgResource.extractStringValue(org.apache.jena.vocabulary.ROV.legalName))
                            .setOrgPath(orgResource.extractStringValue(no.digdir.fdk.parserservice.vocabulary.FDKORG.orgPath))
                            .setOrganisasjonsform(
                                orgResource.extractStringValue(org.apache.jena.vocabulary.ROV.orgType)?.split("#")?.last(),
                            ).setPrefLabel(orgResource.extractLocalizedStrings(org.apache.jena.sparql.vocabulary.FOAF.name))
                        orgBuilder.build()
                    }?.takeIf { it.isNotEmpty() }
            setHasCompetentAuthority(competentAuthorities)
        } else {
            setOwnedBy(
                serviceResource
                    .listResources(CV.ownedBy)
                    ?.mapNotNull { orgResource ->
                        val builder =
                            no.digdir.fdk.model.Organization
                                .newBuilder()
                        builder
                            .setUri(orgResource.extractURIStringValue())
                            .setId(orgResource.extractStringValue(DCTerms.identifier))
                            .setName(orgResource.extractStringValue(org.apache.jena.vocabulary.ROV.legalName))
                            .setOrgPath(orgResource.extractStringValue(no.digdir.fdk.parserservice.vocabulary.FDKORG.orgPath))
                            .setOrganisasjonsform(
                                orgResource.extractStringValue(org.apache.jena.vocabulary.ROV.orgType)?.split("#")?.last(),
                            ).setPrefLabel(orgResource.extractLocalizedStrings(FOAF.name))
                        builder.build()
                    }?.takeIf { it.isNotEmpty() },
            )
        }

        setType(ResourceType.publicservices)
    }
}
