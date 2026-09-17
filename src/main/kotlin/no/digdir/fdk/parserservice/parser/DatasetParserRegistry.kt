package no.digdir.fdk.parserservice.parser

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parserservice.LOGGER
import no.digdir.fdk.parserservice.metrics.ParseMetrics
import no.digdir.fdk.parserservice.model.DcatProfile
import no.fdk.rdf.parse.RdfParseResourceType
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
     * @return Successfully parsed datasets in priority order, and the profiles the dataset is in accordance with
     */
    fun parseWithAllParsers(model: Model, iri: String, fdkId: String): DatasetParseResult {
        val results = mutableListOf<Dataset>()
        val profiles = linkedSetOf<DcatProfile>()

        for (entry in parsers) {
            try {
                val dataset = entry.parser.parse(model, iri, fdkId)
                results.add(dataset)
                entry.parser
                    .dcatProfile()
                    ?.takeIf { entry.parser.appliesTo(model, iri) }
                    ?.let { profiles.add(it) }
                ParseMetrics.recordProfileMatch(RdfParseResourceType.DATASET, entry.name, matched = true)
                LOGGER.debug("Successfully parsed dataset with parser '${entry.name}'")
            } catch (e: Exception) {
                ParseMetrics.recordProfileMatch(RdfParseResourceType.DATASET, entry.name, matched = false)
                LOGGER.warn("Failed to parse dataset with parser '${entry.name}' for $fdkId", e)
            }
        }

        if (results.isEmpty()) {
            throw IllegalStateException("No parsers were able to successfully parse the dataset for $fdkId")
        }

        LOGGER.info("Successfully parsed dataset $fdkId with ${results.size} out of ${parsers.size} parsers")
        return DatasetParseResult(results, profiles.toList())
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
     * Result of parsing a dataset with all registered parsers.
     */
    data class DatasetParseResult(val datasets: List<Dataset>, val dcatProfiles: List<DcatProfile>)

    /**
     * Internal data class for storing parser entries.
     */
    private data class ParserEntry(val parser: DatasetParserStrategy, val priority: Int, val name: String)

    /**
     * Data class for parser information.
     */
    data class ParserInfo(val name: String, val priority: Int)
}
