package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.service.ServiceOutput
import no.digdir.fdk.parserservice.vocabulary.CPSV
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class OutputExtractionTest {
    @Test
    fun extractListOfServiceOutput() {
        val turtle =
            """
            @prefix at:    <http://publications.europa.eu/ontology/authority/> .
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cpsv:produces    <https://test.no/output/1> .

            <https://test.no/output/1>
                a               cv:Output ;
                dct:identifier  "1" ;
                dct:title       "Output name"@en ;
                dct:description "Output description"@en ;
                dct:language    <http://publications.europa.eu/resource/authority/language/ENG> ;
                dct:type        <https://test.no/concept/1> .

            <http://publications.europa.eu/resource/authority/language/ENG>
                a                  skos:Concept;
                at:authority-code  "ENG";
                skos:prefLabel     "English"@en .

            <https://test.no/concept/1>
                a               skos:Concept ;
                skos:prefLabel  "Output type"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceOutput
                    .newBuilder()
                    .setUri("https://test.no/output/1")
                    .setIdentifier("1")
                    .setName(LocalizedStrings().apply { en = "Output name" })
                    .setDescription(LocalizedStrings().apply { en = "Output description" })
                    .setLanguage(
                        listOf(
                            ReferenceDataCode().apply {
                                uri = "http://publications.europa.eu/resource/authority/language/ENG"
                                code = "ENG"
                                prefLabel = LocalizedStrings().apply { en = "English" }
                            },
                        ),
                    ).setType(
                        listOf(
                            ReferenceDataCode().apply {
                                uri = "https://test.no/concept/1"
                                code = "https://test.no/concept/1"
                                prefLabel = LocalizedStrings().apply { en = "Output type" }
                            },
                        ),
                    ).build(),
            )

        assertEquals(expected, subject.extractListOfServiceOutput(CPSV.produces))
    }

    @Test
    fun extractOfServiceOutputIsNullWhenNoRelevantValuesAreExtracted() {
        val turtle =
            """
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cpsv:produces   [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfServiceOutput(CPSV.produces))
    }
}
