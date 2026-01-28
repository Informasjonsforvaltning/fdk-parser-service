package no.digdir.fdk.parserservice.configuration

import jakarta.annotation.PostConstruct
import no.digdir.fdk.parserservice.parser.ConceptParserRegistry
import no.digdir.fdk.parserservice.parser.concept.SkosApNoV1Parser
import no.digdir.fdk.parserservice.parser.concept.SkosApNoV2Parser
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for setting up concept parsers with priority ordering.
 *
 * This configuration automatically registers all available concept parsers
 * with their appropriate priority levels. Higher priority parsers are
 * executed first and their results take precedence.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
open class ConceptParserConfiguration(
    private val parserRegistry: ConceptParserRegistry,
    private val skosApNoV1Parser: SkosApNoV1Parser,
    private val skosApNoV2Parser: SkosApNoV2Parser,
) {
    /**
     * Registers all available concept parsers with their priority levels.
     * This method is called after dependency injection is complete.
     */
    @PostConstruct
    fun registerParsers() {
        // Register V2 parser with highest priority (based on SKOS-AP-NO-V2)
        parserRegistry.registerParser(skosApNoV2Parser, priority = 100, name = "SKOS-AP-NO-V2")

        // Register V1 parser with lowest priority (fallback, based on SKOS-AP-NO-V1)
        parserRegistry.registerParser(skosApNoV1Parser, priority = 50, name = "SKOS-AP-NO-V1")
    }
}
