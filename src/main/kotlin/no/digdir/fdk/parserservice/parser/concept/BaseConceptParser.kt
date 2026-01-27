package no.digdir.fdk.parserservice.parser.concept

import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.concept.Concept
import no.digdir.fdk.parserservice.extract.extractListOfContactPoints
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractOrganization
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.parser.ConceptParserStrategy
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDFS

/**
 * Abstract base class for concept parsers implementing SKOS-AP-NO.
 *
 * This class provides common functionality for parsing concepts
 * and serves as a foundation for version-specific implementations.
 * It handles the extraction of common concept properties that are shared
 * across different versions.
 *
 * @see <a href="https://data.norge.no/specification/skos-ap-no-begrep">SKOS-AP-NO Specification</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 * @see ConceptParserStrategy
 */
abstract class BaseConceptParser : ConceptParserStrategy {
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
     * @return The source format (e.g., "SKOS-AP-NO")
     */
    protected abstract fun getSourceFormat(): String

    /**
     * Adds common concept values to the builder that are shared across all versions.
     *
     * This method extracts and sets common properties such as labels, definitions,
     * relations, and other metadata that are consistent across versions.
     *
     * @param conceptResource The RDF resource representing the concept
     */
    protected fun Concept.Builder.addCommonConceptValues(conceptResource: Resource) {
        setUri(conceptResource.uri)
        setIdentifier(conceptResource.extractStringValue(DCTerms.identifier) ?: conceptResource.uri)
        setType(ResourceType.concept)

        setPublisher(conceptResource.extractOrganization(DCTerms.publisher))

        setContactPoint(conceptResource.extractListOfContactPoints()?.firstOrNull())

        setSeeAlso(conceptResource.extractListOfStrings(RDFS.seeAlso))
        setIsReplacedBy(conceptResource.extractListOfStrings(DCTerms.isReplacedBy))
        setReplaces(conceptResource.extractListOfStrings(DCTerms.replaces))
    }
}
