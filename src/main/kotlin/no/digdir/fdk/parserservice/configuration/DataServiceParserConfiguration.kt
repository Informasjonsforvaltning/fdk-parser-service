package no.digdir.fdk.parserservice.configuration

import no.digdir.fdk.parserservice.parser.DataServiceParserRegistry
import no.digdir.fdk.parserservice.parser.dataservice.DcatApNoV2Parser
import org.springframework.context.annotation.Configuration
import jakarta.annotation.PostConstruct

/**
 * Configuration class for setting up data service parsers with priority ordering.
 *
 * This configuration automatically registers all available data service parsers
 * with their appropriate priority levels. Higher priority parsers are
 * executed first and their results take precedence in the final merged data service.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
open class DataServiceParserConfiguration(
    private val parserRegistry: DataServiceParserRegistry,
    private val dcatApNoV2Parser: DcatApNoV2Parser
) {

    /**
     * Registers all available data service parsers with their priority levels.
     * This method is called after dependency injection is complete.
     */
    @PostConstruct
    fun registerParsers() {
        // Register DCAT-AP-NO v2.2 data service parser
        parserRegistry.registerParser(dcatApNoV2Parser, priority = 50, name = "DCAT-AP-NO-V2")
    }
}
