package no.digdir.fdk.parserservice.parser

import no.digdir.fdk.model.dataset.Dataset
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test class for DatasetParserRegistry.
 */
class DatasetParserRegistryTest {

    private fun minimalDataset(id: String, uri: String): Dataset =
        Dataset.newBuilder()
            .setId(id)
            .setUri(uri)
            .setIdentifier(null)
            .setAdmsIdentifier(null)
            .setHarvest(null)
            .setCatalog(null)
            .setTitle(null)
            .setDescription(null)
            .setDescriptionFormatted(null)
            .setPublisher(null)
            .setDistribution(null)
            .setSample(null)
            .setContactPoint(null)
            .setThemeUris(null)
            .setTheme(null)
            .setLosTheme(null)
            .setEurovocThemes(null)
            .setKeyword(null)
            .setIssued(null)
            .setModified(null)
            .setDctType(null)
            .setAccessRights(null)
            .setLanguage(null)
            .setPage(null)
            .setLandingPage(null)
            .setTemporal(null)
            .setSubject(null)
            .setSpatial(null)
            .setProvenance(null)
            .setAccrualPeriodicity(null)
            .setLegalBasisForAccess(null)
            .setLegalBasisForProcessing(null)
            .setLegalBasisForRestriction(null)
            .setConformsTo(null)
            .setReferences(null)
            .setHasAccuracyAnnotation(null)
            .setHasAvailabilityAnnotation(null)
            .setHasCompletenessAnnotation(null)
            .setHasCurrentnessAnnotation(null)
            .setHasRelevanceAnnotation(null)
            .setQualifiedAttributions(null)
            .setIsOpenData(false)
            .setIsAuthoritative(false)
            .setIsRelatedToTransportportal(false)
            .setInSeries(null)
            .setPrev(null)
            .setLast(null)
            .setDatasetsInSeries(null)
            .setType(null)
            .setSpecializedType(null)
            .build()

    @Test
    fun `should register parsers and execute in priority order`() {
        val registry = DatasetParserRegistry()
        
        // Create mock parsers
        val lowPriorityParser = createMockParser("low-priority", 50)
        val highPriorityParser = createMockParser("high-priority", 100)
        
        // Register parsers
        registry.registerParser(lowPriorityParser, 50, "Low Priority Parser")
        registry.registerParser(highPriorityParser, 100, "High Priority Parser")
        
        // Verify registration
        assertEquals(2, registry.getParserCount())
        val parserInfo = registry.getParserInfo()
        assertEquals(2, parserInfo.size)
        assertTrue(parserInfo.any { it.name == "High Priority Parser" && it.priority == 100 })
        assertTrue(parserInfo.any { it.name == "Low Priority Parser" && it.priority == 50 })
    }

    @Test
    fun `should parse with all parsers and return results in priority order`() {
        val registry = DatasetParserRegistry()
        
        // Create mock parsers that succeed
        val parser1 = createMockParser("result1", 100)
        val parser2 = createMockParser("result2", 50)
        
        registry.registerParser(parser1, 100, "Parser 1")
        registry.registerParser(parser2, 50, "Parser 2")
        
        val model = ModelFactory.createDefaultModel()
        val results = registry.parseWithAllParsers(model, "http://example.org/dataset", "test-id")
        
        assertEquals(2, results.size)
        assertEquals("result1", results[0].id)
        assertEquals("result2", results[1].id)
    }

    @Test
    fun `should handle parser failures gracefully`() {
        val registry = DatasetParserRegistry()
        
        // Create parsers where one fails
        val failingParser = object : DatasetParserStrategy {
            override fun parse(model: Model, iri: String): Dataset {
                throw RuntimeException("Parser failed")
            }
            override fun parse(model: Model, iri: String, fdkId: String): Dataset {
                throw RuntimeException("Parser failed")
            }
        }
        
        val succeedingParser = createMockParser("success", 50)
        
        registry.registerParser(failingParser, 100, "Failing Parser")
        registry.registerParser(succeedingParser, 50, "Succeeding Parser")
        
        val model = ModelFactory.createDefaultModel()
        val results = registry.parseWithAllParsers(model, "http://example.org/dataset", "test-id")
        
        assertEquals(1, results.size)
        assertEquals("success", results[0].id)
    }

    @Test
    fun `should throw exception when no parsers succeed`() {
        val registry = DatasetParserRegistry()
        
        val failingParser = object : DatasetParserStrategy {
            override fun parse(model: Model, iri: String): Dataset {
                throw RuntimeException("Parser failed")
            }
            override fun parse(model: Model, iri: String, fdkId: String): Dataset {
                throw RuntimeException("Parser failed")
            }
        }
        
        registry.registerParser(failingParser, 100, "Failing Parser")
        
        val model = ModelFactory.createDefaultModel()
        assertThrows<IllegalStateException> {
            registry.parseWithAllParsers(model, "http://example.org/dataset", "test-id")
        }
    }

    private fun createMockParser(id: String, priority: Int): DatasetParserStrategy {
        return object : DatasetParserStrategy {
            override fun parse(model: Model, iri: String): Dataset {
                return minimalDataset(id, iri)
            }
            override fun parse(model: Model, iri: String, fdkId: String): Dataset {
                return minimalDataset(id, iri)
            }
        }
    }

    @Test
    fun `results are returned in parser priority order`() {
        val registry = DatasetParserRegistry()

        val lowPriority = object : DatasetParserStrategy {
            override fun parse(model: Model, iri: String): Dataset = minimalDataset("LOW", iri)
            override fun parse(model: Model, iri: String, fdkId: String): Dataset = minimalDataset("LOW", iri)
        }
        val highPriority = object : DatasetParserStrategy {
            override fun parse(model: Model, iri: String): Dataset = minimalDataset("HIGH", iri)
            override fun parse(model: Model, iri: String, fdkId: String): Dataset = minimalDataset("HIGH", iri)
        }

        registry.registerParser(lowPriority, priority = 50, name = "low")
        registry.registerParser(highPriority, priority = 200, name = "high")

        val model = ModelFactory.createDefaultModel()
        val results = registry.parseWithAllParsers(model, "http://example.org/ds", "fdk-id")

        assertEquals(2, results.size)
        assertEquals("HIGH", results[0].id)
        assertEquals("LOW", results[1].id)
    }
}
