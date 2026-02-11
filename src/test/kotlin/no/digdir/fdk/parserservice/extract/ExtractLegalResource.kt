package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.LegalResource
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader

@Tag("unit")
class ExtractLegalResource {
    @Test
    fun extractListOfServiceLegalResources() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix eli:    <http://data.europa.eu/eli/ontology#> .
            @prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .
            @prefix at:     <http://publications.europa.eu/ontology/authority/> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:hasLegalResource <https://test.no/legal/1> .

            <https://test.no/legal/1>
                a               eli:LegalResource ;
                dct:title       "Legal title"@en ;
                dct:language    <http://publications.europa.eu/resource/authority/language/ENG> ;
                dct:description "Legal description"@en ;
                rdfs:seeAlso    <https://test.no/seealso> ;
                dct:type        <https://data.norge.no/vocabulary/legal-resource-type#regulation> ;
                dct:relation    <https://test.no/related> .

            <http://publications.europa.eu/resource/authority/language/ENG>
                a                  skos:Concept;
                at:authority-code  "ENG";
                skos:prefLabel     "English"@en .

            <https://data.norge.no/vocabulary/legal-resource-type#regulation>
                a               eli:ResourceType ;
                skos:prefLabel  "regulation"@en, "forskrift"@nb .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                LegalResource
                    .newBuilder()
                    .setUri("https://test.no/legal/1")
                    .setTitle(LocalizedStrings().apply { en = "Legal title" })
                    .setDctTitle(LocalizedStrings().apply { en = "Legal title" })
                    .setDescription(LocalizedStrings().apply { en = "Legal description" })
                    .setSeeAlso(listOf("https://test.no/seealso"))
                    .setRelation(listOf("https://test.no/related"))
                    .setLanguage(
                        listOf(
                            ReferenceDataCode().apply {
                                uri = "http://publications.europa.eu/resource/authority/language/ENG"
                                code = "ENG"
                                prefLabel = LocalizedStrings().apply { en = "English" }
                            },
                        ),
                    ).setType(
                        ReferenceDataCode().apply {
                            uri = "https://data.norge.no/vocabulary/legal-resource-type#regulation"
                            code = "regulation"
                            prefLabel =
                                LocalizedStrings().apply {
                                    nb = "forskrift"
                                    en = "regulation"
                                }
                        },
                    ).build(),
            )

        assertEquals(expected, subject.extractListOfLegalResources(CV.hasLegalResource))
    }

    @Test
    fun extractOfServiceLegalResourcesIsNullWhenNoRelevantValuesAreExtracted() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:hasLegalResource [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfLegalResources(CV.hasLegalResource))
    }
}
