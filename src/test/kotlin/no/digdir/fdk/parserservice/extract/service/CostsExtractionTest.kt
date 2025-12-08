package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.service.ServiceCost
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class CostsExtractionTest {
    @Test
    fun extractListOfServiceCosts() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cv:hasCost      <https://test.no/cost/1> .

            <https://test.no/cost/1>
                a                   cv:Cost ;
                dct:identifier      "1" ;
                dct:description     "Cost description"@en ;
                cv:currency         <http://publications.europa.eu/resource/authority/currency/NOK> ;
                cv:ifAccessedThrough <https://test.no/channel/1> ;
                cv:value            "100.50"^^xsd:decimal .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceCost
                    .newBuilder()
                    .setUri("https://test.no/cost/1")
                    .setIdentifier("1")
                    .setDescription(LocalizedStrings().apply { en = "Cost description" })
                    .setCurrency("http://publications.europa.eu/resource/authority/currency/NOK")
                    .setCurrencyCode(null)
                    .setIfAccessedThrough("https://test.no/channel/1")
                    .setValue("100.50")
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceCostsV0())
    }

    @Test
    fun extractListOfServiceCostsV1() {
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
                ServiceCost
                    .newBuilder()
                    .setUri("https://test.no/cost/1")
                    .setIdentifier("1")
                    .setDescription(null)
                    .setCurrency(null)
                    .setCurrencyCode(
                        ReferenceDataCode().apply {
                            uri = "http://publications.europa.eu/resource/authority/currency/NOK"
                            code = "NOK"
                            prefLabel = LocalizedStrings().apply { en = "Norwegian krone" }
                        },
                    ).setValue("200.75")
                    .setIfAccessedThrough(null)
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceCostsV1())
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

        assertNull(subject.extractListOfServiceCostsV1())
    }
}
