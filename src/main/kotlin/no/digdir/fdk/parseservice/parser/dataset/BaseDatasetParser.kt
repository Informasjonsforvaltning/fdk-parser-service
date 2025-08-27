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
import no.digdir.fdk.parseservice.extract.extractListOfTemporal
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
import no.digdir.fdk.parseservice.namespace.ADMS
import no.digdir.fdk.parseservice.namespace.EUAT
import no.digdir.fdk.parseservice.namespace.SCHEMA
import no.digdir.fdk.parseservice.parser.DatasetParserStrategy
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.SKOS

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
        setTemporal(datasetResource.extractListOfTemporal(DCTerms.temporal, SCHEMA.startDate, SCHEMA.endDate))
        setSubject(datasetResource.extractListOfSubjects())

        setType(ResourceType.datasets)
    }
}
