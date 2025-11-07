package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.LocalizedStrings
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractLocalizedStrings {
    @Test
    fun localizedStringsExtractHandlesMissingValues() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:DataService .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.DataService).toList().first()

        assertNull(subject.extractLocalizedStrings(DCTerms.title))
    }

    @Test
    fun localizedStringsUsesNorwegianAsFallbackWhenMissingLanguageDecorator() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:DataService ;
                dct:title                 "title" .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.DataService).toList().first()

        val expected = LocalizedStrings().also { it.no = "title" }

        assertEquals(expected, subject.extractLocalizedStrings(DCTerms.title))
    }
}
