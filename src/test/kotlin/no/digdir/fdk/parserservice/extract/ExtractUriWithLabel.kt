package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.UriWithLabel
import no.digdir.fdk.parseservice.extract.extractListOfUriWithLabel
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractUriWithLabel {

    @Test
    fun extractWhenOnlyUri() {
        val turtle = """
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset ;
                dct:conformsTo            <https://conforms.to> .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = listOf(UriWithLabel().apply {
            uri = "https://conforms.to"
        })

        assertEquals(expected, subject.extractListOfUriWithLabel(DCTerms.conformsTo, DCTerms.source, DCTerms.title))
    }

    @Test
    fun extractWhenBlankNode() {
        val turtle = """
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset ;
                dct:conformsTo            [
                    dct:source  <https://source.no> ;
                    dct:title   "Blank node"
                ] .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = listOf(UriWithLabel().apply {
            uri = "https://source.no"
            prefLabel = LocalizedStrings().also { label -> label.no = "Blank node" }
        })

        assertEquals(expected, subject.extractListOfUriWithLabel(DCTerms.conformsTo, DCTerms.source, DCTerms.title))
    }

    @Test
    fun extractWhenUriResource() {
        val turtle = """
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset ;
                dct:conformsTo            <https://conforms.to> .

            <https://conforms.to>
                dct:source  <https://source.no> ;
                dct:title   "URI resource"@en
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = listOf(UriWithLabel().apply {
            uri = "https://source.no"
            prefLabel = LocalizedStrings().also { label -> label.en = "URI resource" }
        })

        assertEquals(expected, subject.extractListOfUriWithLabel(DCTerms.conformsTo, DCTerms.source, DCTerms.title))
    }

    @Test
    fun doesNotThrowErrorWhenLiteral() {
        val turtle = """
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset ;
                dct:conformsTo            "https://conforms.to" .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")

        assertDoesNotThrow { m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first() }
    }
}