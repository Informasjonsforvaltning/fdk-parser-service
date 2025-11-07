package no.digdir.fdk.parserservice.configuration

import jakarta.annotation.PostConstruct
import no.digdir.fdk.parserservice.parser.DatasetParserRegistry
import no.digdir.fdk.parserservice.parser.dataset.DcatApNoV1Parser
import no.digdir.fdk.parserservice.parser.dataset.DcatApNoV2Parser
import no.digdir.fdk.parserservice.parser.dataset.MobilityDcatApV3Parser
import org.springframework.context.annotation.Configuration

/**
 * Configuration class for setting up dataset parsers with priority ordering.
 *
 * This configuration automatically registers all available dataset parsers
 * with their appropriate priority levels. Higher priority parsers are
 * executed first and their results take precedence in the final merged dataset.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
open class DatasetParserConfiguration(
    private val parserRegistry: DatasetParserRegistry,
    private val v1Parser: DcatApNoV1Parser,
    private val v2Parser: DcatApNoV2Parser,
    private val mobilityV3Parser: MobilityDcatApV3Parser,
) {
    /**
     * Registers all available dataset parsers with their priority levels.
     * This method is called after dependency injection is complete.
     */
    @PostConstruct
    fun registerParsers() {
        // Register Mobility V3 parser with highest priority (most specialized, based on DCAT-V3)
        parserRegistry.registerParser(mobilityV3Parser, priority = 200, name = "MOBILITY-DCAT-AP-V3")

        // Register V2 parser with medium priority (based on DCAT-V2)
        parserRegistry.registerParser(v2Parser, priority = 100, name = "DCAT-AP-NO-V2")

        // Register V1 parser with lowest priority (fallback, based on DCAT-V1)
        parserRegistry.registerParser(v1Parser, priority = 50, name = "DCAT-AP-NO-V1")
    }
}
