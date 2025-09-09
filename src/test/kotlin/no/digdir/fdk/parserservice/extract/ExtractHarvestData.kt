package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.HarvestMetaData
import no.digdir.fdk.parseservice.parser.dataset.DcatApNoV1Parser
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractHarvestData {
    val parser = DcatApNoV1Parser()
    val datasetIRI = "https://testdirektoratet.no/model/dataset/0"
    val fdkId = "a1c680ca-62d7-34d5-aa4c-d39b5db033ae"

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

        assertThrows<Exception> { parser.parse(m, datasetIRI, fdkId) }
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

        assertThrows<Exception> { parser.parse(m, datasetIRI, fdkId) }
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

        assertThrows<Exception> { parser.parse(m, datasetIRI, fdkId) }
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
        val parsed = parser.parse(m, datasetIRI, fdkId)

        assertEquals(expected, parsed.harvest)
    }

}