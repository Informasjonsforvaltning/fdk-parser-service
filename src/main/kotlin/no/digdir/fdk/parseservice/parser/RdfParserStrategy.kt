package no.digdir.fdk.parseservice.parser

import no.digdir.fdk.model.dataset.Dataset
import org.apache.jena.rdf.model.Model

/**
 * Strategy interface for parsing RDF models into domain objects.
 * 
 * This interface defines the contract for RDF parsing strategies, allowing
 * different implementations to handle various RDF formats and schemas.
 * 
 * @param T The type of domain object to be parsed from the RDF model
 * 
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
interface RdfParserStrategy<T> {
    /**
     * Parses an RDF model into a domain object of type T.
     * 
     * @param model The Jena RDF model to be parsed
     * @param iri The IRI of the resource to be parsed
     * @return The parsed domain object
     * @throws IllegalArgumentException if the model is null or invalid
     * @throws UnsupportedOperationException if the model format is not supported
     */
    fun parse(model: Model, iri: String): T

    /**
     * Parses an RDF model into a domain object of type T.
     *
     * @param model The Jena RDF model to be parsed
     * @param iri The IRI of the resource to be parsed
     * @param fdkId The FDK ID of the resource to be parsed
     * @return The parsed domain object
     * @throws IllegalArgumentException if the model is null or invalid
     * @throws UnsupportedOperationException if the model format is not supported
     */
    fun parse(model: Model, iri: String, fdkId: String): T
}

/**
 * Type alias for dataset-specific RDF parsing strategies.
 * 
 * This alias provides a more specific interface for parsers that handle
 * dataset resources according to DCAT-AP-NO specifications.
 * 
 * @see RdfParserStrategy
 * @see Dataset
 */
typealias DatasetParserStrategy = RdfParserStrategy<Dataset>
