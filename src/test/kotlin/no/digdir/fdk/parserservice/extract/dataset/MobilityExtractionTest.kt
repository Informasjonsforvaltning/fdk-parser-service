package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.parserservice.parser.dataset.MobilityDcatApV3Parser
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class MobilityExtractionTest {


    @Nested
    internal inner class V1 {
        val datasetIRI = "https://testdirektoratet.no/model/dataset/0"
        val fdkId = "a1c680ca-62d7-34d5-aa4c-d39b5db033ae"
        val parser = MobilityDcatApV3Parser()

        @Test
        fun extractMobilityTheme() {
            val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .
            @prefix mobilitydcatap:   <https://w3id.org/mobilitydcat-ap#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                               dcat:Dataset ;
                mobilitydcatap:mobilityTheme    <https://w3id.org/mobilitydcat-ap/mobility-theme/bike-hiring-availability> .

            <https://w3id.org/mobilitydcat-ap/mobility-theme/bike-hiring-availability>
                a               <http://www.w3.org/2004/02/skos/core#Concept> ;
                skos:inScheme   <https://w3id.org/mobilitydcat-ap/mobility-theme> ;
                skos:prefLabel  "Bike-hiring Availability"@en .
        """.trimIndent()

            val expectedMobilityTheme = listOf(ReferenceDataCode().also { theme ->
                theme.uri = "https://w3id.org/mobilitydcat-ap/mobility-theme/bike-hiring-availability"
                theme.code = "bike-hiring-availability"
                theme.prefLabel = LocalizedStrings().also { label ->
                    label.en = "Bike-hiring Availability"
                }
            })

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, datasetIRI, fdkId)

            assertEquals(expectedMobilityTheme, result.mobilityTheme)
        }
    }
}
