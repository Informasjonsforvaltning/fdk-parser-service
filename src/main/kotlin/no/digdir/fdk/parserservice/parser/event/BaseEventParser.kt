package no.digdir.fdk.parserservice.parser.event

import no.digdir.fdk.model.event.Event
import no.digdir.fdk.parserservice.extract.extractCatalogData
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.parser.EventParserStrategy
import no.digdir.fdk.parserservice.vocabulary.CPSVNO
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS

/**
 * Abstract base class for event parsers.
 *
 * This class provides common functionality for parsing events
 * and serves as a foundation for version-specific implementations.
 * It handles the extraction of common event properties that are shared
 * across different versions.
 *
 * Subclasses must implement the abstract methods to provide version-specific
 * configuration and parsing logic.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 * @see EventParserStrategy
 */
abstract class BaseEventParser : EventParserStrategy {
    /**
     * Gets the default language for this parser version.
     *
     * @return The default language code (e.g., "no", "en")
     */
    protected abstract fun getDefaultLanguage(): String

    /**
     * Gets the version string for this parser.
     *
     * @return The version string (e.g., "0.9")
     */
    protected abstract fun getVersion(): String

    /**
     * Gets the source format identifier for this parser.
     *
     * @return The source format (e.g., "CPSV-AP-NO")
     */
    protected abstract fun getSourceFormat(): String

    /**
     * Gets a list of acceptable RDF types for harvested events.
     *
     * @return List of acceptable RDF types (e.g., [CV.Event, CV.BusinessEvent, CV.LifeEvent])
     */
    protected abstract fun getAcceptableTypes(): List<Resource>

    /**
     * Adds common event values to the builder that are shared across all versions.
     *
     * This method extracts and sets common properties such as title, description,
     * type, relation, and other metadata that are consistent across versions.
     *
     * @param eventResource The RDF resource representing the event
     */
    protected fun Event.Builder.addCommonEventValues(eventResource: Resource) {
        setUri(eventResource.uri)
        setCatalog(eventResource.extractCatalogData())

        setTitle(eventResource.extractLocalizedStrings(DCTerms.title))
        setDescription(eventResource.extractLocalizedStrings(DCTerms.description))

        setIdentifier(eventResource.extractStringValue(DCTerms.identifier))
        setDctType(eventResource.extractListOfReferenceDataCodes(DCTerms.type, "#", SKOS.prefLabel))
        setMayInitiate(eventResource.extractListOfStrings(CPSVNO.mayInitiate))
        setSubject(eventResource.extractListOfStrings(DCTerms.subject))

        val rdfTypes = eventResource.listResources(RDF.type) ?: emptyList()
        when {
            rdfTypes.contains(CV.BusinessEvent) -> setSpecializedType("business_event")
            rdfTypes.contains(CV.LifeEvent) -> setSpecializedType("life_event")
            else -> setSpecializedType(null)
        }
    }
}
