package no.digdir.fdk.parserservice.extract.service

import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader

@Tag("unit")
class ContactPointsExtractionTest {
    @Test
    fun extractListOfServiceContactPoints() {
        val turtle =
            """
            @prefix at:    <http://publications.europa.eu/ontology/authority/> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .
            @prefix vcard:  <http://www.w3.org/2006/vcard/ns#> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:contactPoint  <https://test.no/contact/1> .

            <https://test.no/contact/1>
                a               cv:ContactPoint;
                vcard:category  "Contact type"@en ;
                cv:email        "mailto:test@example.org" ;
                cv:telephone    "tel:+4712345678" ;
                cv:contactPage  <https://test.no/contactpage> ;
                vcard:language  <http://publications.europa.eu/resource/authority/language/ENG> .

            <http://publications.europa.eu/resource/authority/language/ENG>
                a                  skos:Concept;
                at:authority-code  "ENG";
                skos:prefLabel     "English"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val result = subject.extractListOfServiceContactPoints()
        assertEquals(1, result?.size)
        val contact = result?.first()
        assertEquals("https://test.no/contact/1", contact?.uri)
        assertEquals("Contact type", contact?.contactType?.en)
        assertEquals(listOf("+4712345678"), contact?.telephone)
        assertEquals(listOf("test@example.org"), contact?.email)
        assertEquals("https://test.no/contactpage", contact?.contactPage?.first())
    }

    @Test
    fun extractOfServiceContactPointsIsNullWhenNoRelevantValuesAreExtracted() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:contactPoint     [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfServiceContactPoints())
    }
}
