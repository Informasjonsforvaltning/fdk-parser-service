package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.service.ServiceLegalResource
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader

@Tag("unit")
class LegalResourcesExtractionTest {
    @Test
    fun extractListOfServiceLegalResources() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix eli:    <http://data.europa.eu/eli/ontology#> .
            @prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:hasLegalResource <https://test.no/legal/1> .

            <https://test.no/legal/1>
                a               eli:LegalResource ;
                dct:title       "Legal title"@en ;
                dct:description "Legal description"@en ;
                rdfs:seeAlso    <https://test.no/seealso> ;
                dct:relation    <https://test.no/related> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceLegalResource
                    .newBuilder()
                    .setUri("https://test.no/legal/1")
                    .setDctTitle(LocalizedStrings().apply { en = "Legal title" })
                    .setDescription(LocalizedStrings().apply { en = "Legal description" })
                    .setSeeAlso(listOf("https://test.no/seealso"))
                    .setRelation(listOf("https://test.no/related"))
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceLegalResources(CV.hasLegalResource))
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

        assertNull(subject.extractListOfServiceLegalResources(CV.hasLegalResource))
    }
}
