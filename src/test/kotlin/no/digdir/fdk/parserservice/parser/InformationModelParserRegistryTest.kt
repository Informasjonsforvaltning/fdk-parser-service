package no.digdir.fdk.parserservice.parser

import no.digdir.fdk.model.informationmodel.InformationModel
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class InformationModelParserRegistryTest {
    private fun minimalInformationModel(
        id: String,
        uri: String,
    ): InformationModel =
        InformationModel().apply {
            this.id = id
            this.uri = uri
        }

    @Test
    fun `should register parsers and execute in priority order`() {
        val registry = InformationModelParserRegistry()

        val lowPriorityParser = createMockParser("low-priority", 50)
        val highPriorityParser = createMockParser("high-priority", 100)

        registry.registerParser(lowPriorityParser, 50, "Low Priority Parser")
        registry.registerParser(highPriorityParser, 100, "High Priority Parser")

        assertEquals(2, registry.getParserCount())
        val parserInfo = registry.getParserInfo()
        assertEquals(2, parserInfo.size)
        assertTrue(parserInfo.any { it.name == "High Priority Parser" && it.priority == 100 })
        assertTrue(parserInfo.any { it.name == "Low Priority Parser" && it.priority == 50 })
    }

    @Test
    fun `should parse with all parsers and return results in priority order`() {
        val registry = InformationModelParserRegistry()

        val parser1 = createMockParser("result1", 100)
        val parser2 = createMockParser("result2", 50)

        registry.registerParser(parser1, 100, "Parser 1")
        registry.registerParser(parser2, 50, "Parser 2")

        val model = ModelFactory.createDefaultModel()
        val results = registry.parseWithAllParsers(model, "http://example.org/information-model", "test-id")

        assertEquals(2, results.size)
        assertEquals("result1", results[0].id)
        assertEquals("result2", results[1].id)
    }

    @Test
    fun `should handle parser failures gracefully`() {
        val registry = InformationModelParserRegistry()

        val failingParser =
            object : InformationModelParserStrategy {
                override fun parse(
                    model: Model,
                    iri: String,
                ): InformationModel = throw RuntimeException("Parser failed")

                override fun parse(
                    model: Model,
                    iri: String,
                    fdkId: String,
                ): InformationModel = throw RuntimeException("Parser failed")
            }

        val succeedingParser = createMockParser("success", 50)

        registry.registerParser(failingParser, 100, "Failing Parser")
        registry.registerParser(succeedingParser, 50, "Succeeding Parser")

        val model = ModelFactory.createDefaultModel()
        val results = registry.parseWithAllParsers(model, "http://example.org/information-model", "test-id")

        assertEquals(1, results.size)
        assertEquals("success", results[0].id)
    }

    @Test
    fun `should throw exception when no parsers succeed`() {
        val registry = InformationModelParserRegistry()

        val failingParser =
            object : InformationModelParserStrategy {
                override fun parse(
                    model: Model,
                    iri: String,
                ): InformationModel = throw RuntimeException("Parser failed")

                override fun parse(
                    model: Model,
                    iri: String,
                    fdkId: String,
                ): InformationModel = throw RuntimeException("Parser failed")
            }

        registry.registerParser(failingParser, 100, "Failing Parser")

        val model = ModelFactory.createDefaultModel()
        assertThrows<IllegalStateException> {
            registry.parseWithAllParsers(model, "http://example.org/information-model", "test-id")
        }
    }

    private fun createMockParser(
        id: String,
        priority: Int,
    ): InformationModelParserStrategy =
        object : InformationModelParserStrategy {
            override fun parse(
                model: Model,
                iri: String,
            ): InformationModel = minimalInformationModel(id, iri)

            override fun parse(
                model: Model,
                iri: String,
                fdkId: String,
            ): InformationModel = minimalInformationModel(id, iri)
        }
}
