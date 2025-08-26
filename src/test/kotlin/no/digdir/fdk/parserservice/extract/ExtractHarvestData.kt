package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.HarvestMetaData
import no.digdir.fdk.parseservice.extract.extractHarvestMetaData
import no.digdir.fdk.parseservice.extract.fdkRecord
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractHarvestData {

    @Test
    fun exceptionWhenGraphIsMissingCatalogRecord() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                    dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                    foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")

        assertThrows<Exception> { m.fdkRecord(listOf(DCAT.Dataset)) }
    }

    @Test
    fun exceptionWhenGraphHasMultipleRecords() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                    foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset .

            <http://test.fellesdatakatalog.digdir.no/datasets/b1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "b1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                    foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/1> .

            <https://testdirektoratet.no/model/dataset/1>
                a                         dcat:Dataset .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")

        assertThrows<Exception> { m.fdkRecord(listOf(DCAT.Dataset)) }
    }

    @Test
    fun exceptionWhenCatalogRecordIsMissingPrimaryTopic() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")

        assertThrows<Exception> { m.fdkRecord(listOf(DCAT.Dataset)) }
    }

    @Test
    fun exceptionWhenPrimaryTopicDoesNotHaveAcceptableType() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                    foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:DataService .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")

        assertThrows<Exception> { m.fdkRecord(listOf(DCAT.Dataset)) }
    }

    @Test
    fun exceptionWhenCatalogRecordHasMultiplePrimaryTopics() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                    foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> , <https://testdirektoratet.no/model/dataset/1> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset .

            <https://testdirektoratet.no/model/dataset/1>
                a                         dcat:Dataset .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")

        assertThrows<Exception> { m.fdkRecord(listOf(DCAT.Dataset)) }
    }

    @Test
    fun correctlyExtractHarvestMetaData() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                    dct:issued         "2018-01-11T10:50:10.111Z"^^xsd:dateTime ;
                    dct:modified       "2020-02-22T12:52:20.222Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset .
        """.trimIndent()

        val expected = HarvestMetaData().also {
            it.firstHarvested = "2018-01-11T10:50:10.111Z"
            it.modified = "2020-02-22T12:52:20.222Z"
        }

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.CatalogRecord).toList().first()

        assertEquals(expected, subject.extractHarvestMetaData())
    }

}