package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.Catalog
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.Organization
import no.digdir.fdk.parseservice.extract.extractCatalogData
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class ExtractCatalog {

    @Test
    fun extractDatasetCatalog() {
        val turtle = """
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .
            @prefix foaf:   <http://xmlns.com/foaf/0.1/> .
            
            <https://testdirektoratet.no/model/catalog>
                a               dcat:Catalog ;
                dct:identifier  <https://id.no> ;
                dcat:dataset    <https://testdirektoratet.no/model/dataset/0> ;
                dct:title       "Katalog"@no ;
                dct:description "Beskrivelse av katalog"@no ;
                dct:publisher   <https://testdirektoratet.no/publisher> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset .

            <https://testdirektoratet.no/publisher>
                a               foaf:Agent ;
                dct:identifier  "112233445" .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expectedPublisher = Organization().also {
            it.uri = "https://testdirektoratet.no/publisher"
            it.id = "112233445"
        }

        val expected = Catalog().also {
            it.uri = "https://testdirektoratet.no/model/catalog"
            it.id = "https://id.no"
            it.title = LocalizedStrings().also { label -> label.no = "Katalog" }
            it.description = LocalizedStrings().also { label -> label.no = "Beskrivelse av katalog" }
            it.publisher = expectedPublisher
        }

        assertEquals(expected, subject.extractCatalogData())
    }

    @Test
    fun handlesMultipleCatalogs() {
        val turtle = """
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .
            @prefix foaf:   <http://xmlns.com/foaf/0.1/> .
            
            <https://testdirektoratet.no/model/catalog/0>
                a               dcat:Catalog ;
                dcat:dataset    <https://testdirektoratet.no/model/dataset/0> .
            
            <https://testdirektoratet.no/model/catalog/1>
                a               dcat:Catalog ;
                dcat:dataset    <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset .

            <https://testdirektoratet.no/publisher>
                a               foaf:Agent ;
                dct:identifier  "112233445" .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val oneOfExpectedURIs = listOf(
            "https://testdirektoratet.no/model/catalog/0",
            "https://testdirektoratet.no/model/catalog/1"
        )

        val extractedCatalog = subject.extractCatalogData()

        assertTrue(oneOfExpectedURIs.any { it == extractedCatalog!!.uri })
    }

}