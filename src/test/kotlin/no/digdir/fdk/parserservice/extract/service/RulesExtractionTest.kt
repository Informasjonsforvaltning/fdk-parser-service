package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.service.ServiceRule
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class RulesExtractionTest {
    @Test
    fun extractListOfServiceRules() {
        val turtle =
            """
            @prefix at:    <http://publications.europa.eu/ontology/authority/> .
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cpsv:follows    <https://test.no/rule/1> .

            <https://test.no/rule/1>
                a               cpsv:Rule ;
                dct:identifier  "1" ;
                dct:title       "Rule name"@en ;
                dct:description "Rule description"@en ;
                dct:language    <http://publications.europa.eu/resource/authority/language/ENG> ;
                cpsv:implements <https://test.no/legal/1> .

            <http://publications.europa.eu/resource/authority/language/ENG>
                a                  skos:Concept;
                at:authority-code  "ENG";
                skos:prefLabel     "English"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceRule
                    .newBuilder()
                    .setUri("https://test.no/rule/1")
                    .setIdentifier("1")
                    .setName(LocalizedStrings().apply { en = "Rule name" })
                    .setDescription(LocalizedStrings().apply { en = "Rule description" })
                    .setLanguage(
                        listOf(
                            ReferenceDataCode().apply {
                                uri = "http://publications.europa.eu/resource/authority/language/ENG"
                                code = "ENG"
                                prefLabel = LocalizedStrings().apply { en = "English" }
                            },
                        ),
                    ).setLegalResources(listOf("https://test.no/legal/1"))
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceRules())
    }

    @Test
    fun extractOfServiceRulesIsNullWhenNoRelevantValuesAreExtracted() {
        val turtle =
            """
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cpsv:follows    [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfServiceRules())
    }
}
