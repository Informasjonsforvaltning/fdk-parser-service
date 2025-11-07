package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.dataset.InSeries
import no.digdir.fdk.parserservice.vocabulary.DCAT3
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractDatasetSeriesData {
    @Test
    fun extractInSeries() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <http://test.fellesdatakatalog.digdir.no/datasets/b1c680cb-62d7-34d5-bb4c-d39b5db033be>
                a                  dcat:CatalogRecord ;
                dct:identifier     "b1c680cb-62d7-34d5-bb4c-d39b5db033be" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/series/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset ;
                dct:title       "Dataset in series"@en ;
                dcat:inSeries   <https://testdirektoratet.no/model/series/0> .

            <https://testdirektoratet.no/model/series/0>
                a               dcat:Dataset , dcat:DatasetSeries ;
                dct:title       "Series"@en ;
                dcat:last       <https://testdirektoratet.no/model/dataset/1> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected =
            InSeries().apply {
                id = "b1c680cb-62d7-34d5-bb4c-d39b5db033be"
                uri = "https://testdirektoratet.no/model/series/0"
                title = LocalizedStrings().apply { en = "Series" }
            }

        assertEquals(expected, subject.extractInSeries())
    }

    @Test
    fun extractDatasetSeriesHandlesCyclicReferences() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/b1c680cb-62d7-34d5-bb4c-d39b5db033be>
                a                  dcat:CatalogRecord ;
                dct:identifier     "b1c680cb-62d7-34d5-bb4c-d39b5db033be" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/series/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset ;
                dcat:prev       <https://testdirektoratet.no/model/dataset/1> .

            <https://testdirektoratet.no/model/dataset/1>
                a               dcat:Dataset ;
                dcat:prev       <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/series/0>
                a               dcat:Dataset , dcat:DatasetSeries ;
                dcat:last       <https://testdirektoratet.no/model/dataset/1> .
            """.trimIndent()

        val expected =
            listOf(
                "https://testdirektoratet.no/model/dataset/1",
                "https://testdirektoratet.no/model/dataset/0",
            )

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT3.DatasetSeries).toList().first()

        assertEquals(expected, subject.extractListOfDatasetsInSeries())
    }
}
