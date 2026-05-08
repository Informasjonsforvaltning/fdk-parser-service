package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.Cost
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.parserservice.extract.extractListOfCosts
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractDatasetCosts {
    @Test
    fun `should extract single dataset cost`() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix cv:    <http://data.europa.eu/m8g/> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dc:    <http://purl.org/dc/elements/1.1/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <https://test.no/dataset/1>
                a               dcat:Dataset ;
                cv:hasCost      <https://test.no/cost/1> .

            <https://test.no/cost/1>
                a               cv:Cost ;
                cv:hasValue     "100.50"^^xsd:decimal ;
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
        val subject = m.getResource("https://test.no/dataset/1")

        val expected =
            listOf(
                Cost
                    .newBuilder()
                    .setIdentifier(null)
                    .setHasValue("100.50")
                    .setDescription(LocalizedStrings().apply { nb = "Kostnadsbeskrivelse" })
                    .setDocumentation(listOf("https://example.com/cost-info"))
                    .setCurrency(
                        ReferenceDataCode().apply {
                            uri = "http://publications.europa.eu/resource/authority/currency/NOK"
                            code = "NOK"
                            prefLabel = LocalizedStrings().apply { en = "Norwegian krone" }
                        },
                    ).setIsDefinedBy(null)
                    .build(),
            )

        assertEquals(expected, subject.extractListOfCosts())
    }

    @Test
    fun `should extract dataset cost with identifier`() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix cv:    <http://data.europa.eu/m8g/> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dc:    <http://purl.org/dc/elements/1.1/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <https://test.no/dataset/1>
                a               dcat:Dataset ;
                cv:hasCost      <https://test.no/cost/1> .

            <https://test.no/cost/1>
                a               cv:Cost ;
                dct:identifier  "cost-42" ;
                cv:hasValue     "200.75"^^xsd:double ;
                cv:currency     <http://publications.europa.eu/resource/authority/currency/EUR> .

            <http://publications.europa.eu/resource/authority/currency/EUR>
                a               skos:Concept ;
                dc:identifier   "EUR" ;
                skos:prefLabel  "Euro"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/dataset/1")

        val expected =
            listOf(
                Cost
                    .newBuilder()
                    .setIdentifier("cost-42")
                    .setHasValue("200.75")
                    .setDescription(null)
                    .setDocumentation(null)
                    .setCurrency(
                        ReferenceDataCode().apply {
                            uri = "http://publications.europa.eu/resource/authority/currency/EUR"
                            code = "EUR"
                            prefLabel = LocalizedStrings().apply { en = "Euro" }
                        },
                    ).setIsDefinedBy(null)
                    .build(),
            )

        assertEquals(expected, subject.extractListOfCosts())
    }

    @Test
    fun `should extract multiple dataset costs`() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix cv:    <http://data.europa.eu/m8g/> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dc:    <http://purl.org/dc/elements/1.1/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <https://test.no/dataset/1>
                a               dcat:Dataset ;
                cv:hasCost      <https://test.no/cost/1> ,
                                <https://test.no/cost/2> .

            <https://test.no/cost/1>
                a               cv:Cost ;
                cv:hasValue     "100"^^xsd:decimal ;
                cv:currency     <http://publications.europa.eu/resource/authority/currency/NOK> .

            <https://test.no/cost/2>
                a               cv:Cost ;
                cv:hasValue     "200"^^xsd:decimal ;
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
        val subject = m.getResource("https://test.no/dataset/1")

        val result = subject.extractListOfCosts()
        assertEquals(2, result?.size)
    }

    @Test
    fun `should return null when no costs exist`() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix cv:    <http://data.europa.eu/m8g/> .

            <https://test.no/dataset/1>
                a               dcat:Dataset ;
                cv:hasCost      [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/dataset/1")

        assertNull(subject.extractListOfCosts())
    }

    @Test
    fun `should return null when dataset has no cost property`() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .

            <https://test.no/dataset/1>
                a               dcat:Dataset .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/dataset/1")

        assertNull(subject.extractListOfCosts())
    }

    @Test
    fun `should not include isDefinedBy for dataset costs`() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix cv:    <http://data.europa.eu/m8g/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix org:   <http://www.w3.org/ns/org#> .

            <https://test.no/dataset/1>
                a               dcat:Dataset ;
                cv:hasCost      <https://test.no/cost/1> .

            <https://test.no/cost/1>
                a               cv:Cost ;
                cv:hasValue     "50"^^xsd:decimal ;
                cv:isDefinedBy  <https://test.no/org/1> .

            <https://test.no/org/1>
                a               org:Organization .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/dataset/1")

        val result = subject.extractListOfCosts()
        assertEquals(1, result?.size)
        assertNull(result?.first()?.isDefinedBy)
    }
}
