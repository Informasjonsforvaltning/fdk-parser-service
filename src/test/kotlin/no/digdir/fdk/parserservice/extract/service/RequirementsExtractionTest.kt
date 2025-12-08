package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.UriWithLabel
import no.digdir.fdk.model.service.ServiceRequirement
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class RequirementsExtractionTest {
    @Test
    fun extractListOfServiceRequirements() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:holdsRequirement <https://test.no/requirement/1> .

            <https://test.no/requirement/1>
                a               cv:Requirement ;
                dct:identifier  "1" ;
                dct:title       "Requirement title"@en ;
                dct:description "Requirement description"@en ;
                dct:type        <https://test.no/concept/1> ;
                cv:fulfils      <https://test.no/rule/1> .

            <https://test.no/concept/1>
                a               skos:Concept ;
                dct:source      <https://test.no/source> ;
                skos:prefLabel "Concept"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceRequirement
                    .newBuilder()
                    .setUri("https://test.no/requirement/1")
                    .setIdentifier("1")
                    .setDctTitle(LocalizedStrings().apply { en = "Requirement title" })
                    .setDescription(LocalizedStrings().apply { en = "Requirement description" })
                    .setDctType(
                        listOf(
                            UriWithLabel().apply {
                                uri = "https://test.no/source"
                                prefLabel = LocalizedStrings().apply { en = "Concept" }
                            },
                        ),
                    ).setFulfils(listOf("https://test.no/rule/1"))
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceRequirements())
    }

    @Test
    fun extractOfServiceRequirementsIsNullWhenNoRelevantValuesAreExtracted() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:holdsRequirement [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfServiceRequirements())
    }
}
