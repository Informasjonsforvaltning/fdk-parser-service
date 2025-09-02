package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.Temporal
import no.digdir.fdk.parseservice.extract.extractListOfTemporal
import no.digdir.fdk.parseservice.vocabulary.SCHEMA
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractTemporal {

    @Test
    fun extractListOfTemporal() {
        val turtle = """
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .
            @prefix schema:  <http://schema.org/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset ;
                dct:temporal    [ a                   dct:PeriodOfTime ;
                                  schema:startDate    "2019-04-02"^^xsd:date ;
                                  schema:endDate      "2020-04-02"^^xsd:date
                                ] ,
                                <https://temporal.no> .

            <https://temporal.no>
                a                   dct:PeriodOfTime ;
                schema:startDate    "2021-04-02"^^xsd:date ;
                schema:endDate      "2022-04-02"^^xsd:date .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = listOf(
            Temporal().apply {
                uri = "https://temporal.no"
                startDate = "2021-04-02"
                endDate = "2022-04-02"
            },
            Temporal().apply {
                startDate = "2019-04-02"
                endDate = "2020-04-02"
            }
        )

        assertEquals(expected, subject.extractListOfTemporal(DCTerms.temporal, SCHEMA.startDate, SCHEMA.endDate))
    }
}