package no.digdir.fdk.parserservice.parser

import no.digdir.fdk.model.event.Event
import no.digdir.fdk.parserservice.LOGGER
import org.apache.jena.rdf.model.Model
import org.springframework.stereotype.Component

/**
 * Registry for managing event parsers with priority ordering.
 *
 * This registry allows for dynamic registration and execution of event parsers
 * in a prioritized manner. Parsers are executed in priority order, and the first
 * successful parse is used as the primary result, with others as fallbacks.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class EventParserRegistry {
    private val parsers = mutableListOf<ParserEntry>()

    /**
     * Registers a parser with a specific priority.
     * Higher priority values are executed first.
     *
     * @param parser The parser to register
     * @param priority The priority level (higher = more priority)
     * @param name Human-readable name for logging
     */
    fun registerParser(
        parser: EventParserStrategy,
        priority: Int,
        name: String,
    ) {
        parsers.add(ParserEntry(parser, priority, name))
        parsers.sortByDescending { it.priority }
        LOGGER.info("Registered event parser '$name' with priority $priority")
    }

    /**
     * Parses an event using all registered parsers in priority order.
     *
     * @param model The RDF model to parse
     * @param iri The IRI of the resource to parse
     * @param fdkId The FDK ID of the resource
     * @return List of successfully parsed events in priority order
     */
    fun parseWithAllParsers(
        model: Model,
        iri: String,
        fdkId: String,
    ): List<Event> {
        val results = mutableListOf<Event>()

        for (entry in parsers) {
            try {
                val event = entry.parser.parse(model, iri, fdkId)
                results.add(event)
                LOGGER.debug("Successfully parsed event with parser '${entry.name}'")
            } catch (e: Exception) {
                LOGGER.warn("Failed to parse event with parser '${entry.name}' for $fdkId", e)
            }
        }

        if (results.isEmpty()) {
            throw IllegalStateException("No parsers were able to successfully parse the event for $fdkId")
        }

        LOGGER.info("Successfully parsed event $fdkId with ${results.size} out of ${parsers.size} parsers")
        return results
    }

    /**
     * Gets the number of registered parsers.
     */
    fun getParserCount(): Int = parsers.size

    /**
     * Gets information about registered parsers.
     */
    fun getParserInfo(): List<ParserInfo> =
        parsers.map {
            ParserInfo(it.name, it.priority)
        }

    /**
     * Internal data class for storing parser entries.
     */
    private data class ParserEntry(
        val parser: EventParserStrategy,
        val priority: Int,
        val name: String,
    )

    /**
     * Data class for parser information.
     */
    data class ParserInfo(
        val name: String,
        val priority: Int,
    )
}
