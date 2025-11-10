package no.digdir.fdk.parserservice.configuration

import jakarta.annotation.PostConstruct
import no.digdir.fdk.parserservice.parser.InformationModelParserRegistry
import no.digdir.fdk.parserservice.parser.informationmodel.ModellDcatApNoV1Parser
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for setting up information model parsers with priority ordering.
 *
 * This configuration automatically registers all available information model parsers
 * with their appropriate priority levels. Higher priority parsers are
 * executed first and their results take precedence in the final merged information model.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
open class InformationModelParserConfiguration(
    private val parserRegistry: InformationModelParserRegistry,
    private val v1Parser: ModellDcatApNoV1Parser,
) {
    /**
     * Registers all available information model parsers with their priority levels.
     * This method is called after dependency injection is complete.
     */
    @PostConstruct
    fun registerParsers() {
        // Register V2 parser with standard priority
        parserRegistry.registerParser(v1Parser, priority = 100, name = "ModellDCAT-AP-NO-V1")
    }
}
