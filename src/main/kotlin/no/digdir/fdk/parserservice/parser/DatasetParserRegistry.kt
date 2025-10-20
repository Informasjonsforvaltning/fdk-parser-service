package no.digdir.fdk.parserservice.parser

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parserservice.LOGGER
import org.apache.jena.rdf.model.Model
import org.springframework.stereotype.Component

/**
 * Registry for managing dataset parsers with priority ordering.
 * 
 * This registry allows for dynamic registration and execution of dataset parsers
 * in a prioritized manner. Parsers are executed in priority order, and the first
 * successful parse is used as the primary result, with others as fallbacks.
 * 
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class DatasetParserRegistry {
    
    private val parsers = mutableListOf<ParserEntry>()
    
    /**
     * Registers a parser with a specific priority.
     * Higher priority values are executed first.
     * 
     * @param parser The parser to register
     * @param priority The priority level (higher = more priority)
     * @param name Human-readable name for logging
     */
    fun registerParser(parser: DatasetParserStrategy, priority: Int, name: String) {
        parsers.add(ParserEntry(parser, priority, name))
        parsers.sortByDescending { it.priority }
        LOGGER.info("Registered dataset parser '$name' with priority $priority")
    }
    
    /**
     * Parses a dataset using all registered parsers in priority order.
     * 
     * @param model The RDF model to parse
     * @param iri The IRI of the resource to parse
     * @param fdkId The FDK ID of the resource
     * @return List of successfully parsed datasets in priority order
     */
    fun parseWithAllParsers(model: Model, iri: String, fdkId: String): List<Dataset> {
        val results = mutableListOf<Dataset>()
        
        for (entry in parsers) {
            try {
                val dataset = entry.parser.parse(model, iri, fdkId)
                results.add(dataset)
                LOGGER.debug("Successfully parsed dataset with parser '${entry.name}'")
            } catch (e: Exception) {
                LOGGER.warn("Failed to parse dataset with parser '${entry.name}' for $fdkId", e)
            }
        }
        
        if (results.isEmpty()) {
            throw IllegalStateException("No parsers were able to successfully parse the dataset for $fdkId")
        }
        
        LOGGER.info("Successfully parsed dataset $fdkId with ${results.size} out of ${parsers.size} parsers")
        return results
    }
    
    /**
     * Gets the number of registered parsers.
     */
    fun getParserCount(): Int = parsers.size
    
    /**
     * Gets information about registered parsers.
     */
    fun getParserInfo(): List<ParserInfo> = parsers.map { 
        ParserInfo(it.name, it.priority) 
    }
    
    /**
     * Internal data class for storing parser entries.
     */
    private data class ParserEntry(
        val parser: DatasetParserStrategy,
        val priority: Int,
        val name: String
    )
    
    /**
     * Data class for parser information.
     */
    data class ParserInfo(
        val name: String,
        val priority: Int
    )
}
