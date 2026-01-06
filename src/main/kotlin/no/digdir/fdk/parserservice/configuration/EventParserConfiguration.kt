package no.digdir.fdk.parserservice.configuration

import jakarta.annotation.PostConstruct
import no.digdir.fdk.parserservice.parser.EventParserRegistry
import no.digdir.fdk.parserservice.parser.event.CpsvApNoV0Parser
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for setting up event parsers with priority ordering.
 *
 * This configuration automatically registers all available event parsers
 * with their appropriate priority levels. Higher priority parsers are
 * executed first and their results take precedence.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
open class EventParserConfiguration(
    private val parserRegistry: EventParserRegistry,
    private val cpsvApNoV0Parser: CpsvApNoV0Parser,
) {
    /**
     * Registers all available event parsers with their priority levels.
     * This method is called after dependency injection is complete.
     */
    @PostConstruct
    fun registerParsers() {
        parserRegistry.registerParser(cpsvApNoV0Parser, priority = 50, name = "CPSV-AP-NO-V0")
    }
}
