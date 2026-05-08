package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.Cost
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.parserservice.extract.extractListOfServiceCosts
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Tag("unit")
class CostsExtractionTest {
    @Test
    fun extractListOfServiceCosts() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix dc:     <http://purl.org/dc/elements/1.1/> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cv:hasCost      <https://test.no/cost/1> .

            <https://test.no/cost/1>
                a                   cv:Cost ;
                dct:identifier      "1" ;
                cv:currency         <http://publications.europa.eu/resource/authority/currency/NOK> ;
                cv:hasValue         "200.75"^^xsd:double .

            <http://publications.europa.eu/resource/authority/currency/NOK>
                a               skos:Concept ;
                dc:identifier   "NOK" ;
                skos:prefLabel  "Norwegian krone"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                Cost
                    .newBuilder()
                    .setIdentifier("1")
                    .setHasValue("200.75")
                    .setDescription(null)
                    .setDocumentation(null)
                    .setCurrency(
                        ReferenceDataCode().apply {
                            uri = "http://publications.europa.eu/resource/authority/currency/NOK"
                            code = "NOK"
                            prefLabel = LocalizedStrings().apply { en = "Norwegian krone" }
                        },
                    ).setIsDefinedBy(null)
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceCosts())
    }

    @Test
    fun `isDefinedBy is extracted for service costs`() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .
            @prefix org:    <http://www.w3.org/ns/org#> .
            @prefix foaf:   <http://xmlns.com/foaf/0.1/> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cv:hasCost      <https://test.no/cost/1> .

            <https://test.no/cost/1>
                a               cv:Cost ;
                cv:hasValue     "100"^^xsd:double ;
                cv:isDefinedBy  <https://test.no/org/1> .

            <https://test.no/org/1>
                a               org:Organization ;
                foaf:name       "Test Organization" .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val result = subject.extractListOfServiceCosts()
        assertEquals(1, result?.size)

        val isDefinedBy = result?.first()?.isDefinedBy
        assertNotNull(isDefinedBy)
        assertEquals(1, isDefinedBy.size)
        assertEquals("https://test.no/org/1", isDefinedBy.first().uri)
        assertEquals("Test Organization", isDefinedBy.first().name)
    }

    @Test
    fun extractListOfServiceCostsIsNullWhenNoRelevantValuesAreExtracted() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cv:hasCost      [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfServiceCosts())
    }
}
