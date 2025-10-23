package no.digdir.fdk.parserservice.parser

import no.digdir.fdk.model.dataservice.DataService
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test class for DataServiceParserRegistry.
 */
@Tag("unit")
class DataServiceParserRegistryTest {

    private fun minimalDataService(id: String, uri: String): DataService =
        DataService().apply {
            this.id = id
            this.uri = uri
        }

    @Test
    fun `should register parsers and execute in priority order`() {
        val registry = DataServiceParserRegistry()
        
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
        val registry = DataServiceParserRegistry()
        
        // Create mock parsers that succeed
        val parser1 = createMockParser("result1", 100)
        val parser2 = createMockParser("result2", 50)
        
        registry.registerParser(parser1, 100, "Parser 1")
        registry.registerParser(parser2, 50, "Parser 2")
        
        val model = ModelFactory.createDefaultModel()
        val results = registry.parseWithAllParsers(model, "http://example.org/data-service", "test-id")
        
        assertEquals(2, results.size)
        assertEquals("result1", results[0].id)
        assertEquals("result2", results[1].id)
    }

    @Test
    fun `should handle parser failures gracefully`() {
        val registry = DataServiceParserRegistry()
        
        // Create parsers where one fails
        val failingParser = object : DataServiceParserStrategy {
            override fun parse(model: Model, iri: String): DataService {
                throw RuntimeException("Parser failed")
            }
            override fun parse(model: Model, iri: String, fdkId: String): DataService {
                throw RuntimeException("Parser failed")
            }
        }
        
        val succeedingParser = createMockParser("success", 50)
        
        registry.registerParser(failingParser, 100, "Failing Parser")
        registry.registerParser(succeedingParser, 50, "Succeeding Parser")
        
        val model = ModelFactory.createDefaultModel()
        val results = registry.parseWithAllParsers(model, "http://example.org/data-service", "test-id")
        
        assertEquals(1, results.size)
        assertEquals("success", results[0].id)
    }

    @Test
    fun `should throw exception when no parsers succeed`() {
        val registry = DataServiceParserRegistry()

        val failingParser = object : DataServiceParserStrategy {
            override fun parse(model: Model, iri: String): DataService {
                throw RuntimeException("Parser failed")
            }
            override fun parse(model: Model, iri: String, fdkId: String): DataService {
                throw RuntimeException("Parser failed")
            }
        }

        registry.registerParser(failingParser, 100, "Failing Parser")
        
        val model = ModelFactory.createDefaultModel()
        assertThrows<IllegalStateException> {
            registry.parseWithAllParsers(model, "http://example.org/data-service", "test-id")
        }
    }

    private fun createMockParser(id: String, priority: Int): DataServiceParserStrategy {
        return object : DataServiceParserStrategy {
            override fun parse(model: Model, iri: String): DataService {
                return minimalDataService(id, iri)
            }
            override fun parse(model: Model, iri: String, fdkId: String): DataService {
                return minimalDataService(id, iri)
            }
        }
    }
}
