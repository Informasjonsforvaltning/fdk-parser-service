package no.digdir.fdk.parserservice.extract.dataservice

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.dataservice.DataServiceCost
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class DataServiceCostExtractionTest {
    @Test
    fun `should extract single data service cost`() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix cv:    <http://data.europa.eu/m8g/> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dc:    <http://purl.org/dc/elements/1.1/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <https://test.no/data-service/1>
                a               dcat:DataService ;
                cv:hasCost      <https://test.no/cost/1> .

            <https://test.no/cost/1>
                a               cv:Cost ;
                cv:hasValue        "100.50"^^xsd:decimal ;
                dct:description "Kostnadsbeskrivelse"@nb ;
                foaf:page       <https://example.com/cost-info> ;
                cv:currency     <http://publications.europa.eu/resource/authority/currency/NOK> .

            <http://publications.europa.eu/resource/authority/currency/NOK>
                a               skos:Concept ;
                dc:identifier   "NOK" ;
                skos:prefLabel  "Norwegian krone"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/data-service/1")

        val expected =
            listOf(
                DataServiceCost
                    .newBuilder()
                    .setHasValue("100.50")
                    .setDescription(LocalizedStrings().apply { nb = "Kostnadsbeskrivelse" })
                    .setDocumentation(listOf("https://example.com/cost-info"))
                    .setCurrency(
                        ReferenceDataCode().apply {
                            uri = "http://publications.europa.eu/resource/authority/currency/NOK"
                            code = "NOK"
                            prefLabel = LocalizedStrings().apply { en = "Norwegian krone" }
                        },
                    ).build(),
            )

        assertEquals(expected, subject.extractListOfDataServiceCosts())
    }

    @Test
    fun `should extract multiple data service costs`() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix cv:    <http://data.europa.eu/m8g/> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dc:    <http://purl.org/dc/elements/1.1/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <https://test.no/data-service/1>
                a               dcat:DataService ;
                cv:hasCost      <https://test.no/cost/1> ,
                                <https://test.no/cost/2> .

            <https://test.no/cost/1>
                a               cv:Cost ;
                cv:hasValue        "100"^^xsd:decimal ;
                cv:currency     <http://publications.europa.eu/resource/authority/currency/NOK> .

            <https://test.no/cost/2>
                a               cv:Cost ;
                cv:hasValue        "200"^^xsd:decimal ;
                cv:currency     <http://publications.europa.eu/resource/authority/currency/EUR> .

            <http://publications.europa.eu/resource/authority/currency/NOK>
                a               skos:Concept ;
                dc:identifier   "NOK" ;
                skos:prefLabel  "Norwegian krone"@en .

            <http://publications.europa.eu/resource/authority/currency/EUR>
                a               skos:Concept ;
                dc:identifier   "EUR" ;
                skos:prefLabel  "Euro"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/data-service/1")

        val result = subject.extractListOfDataServiceCosts()
        assertEquals(2, result?.size)
    }

    @Test
    fun `should return null when no costs exist`() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix cv:    <http://data.europa.eu/m8g/> .

            <https://test.no/data-service/1>
                a               dcat:DataService ;
                cv:hasCost      [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/data-service/1")

        assertNull(subject.extractListOfDataServiceCosts())
    }
}
