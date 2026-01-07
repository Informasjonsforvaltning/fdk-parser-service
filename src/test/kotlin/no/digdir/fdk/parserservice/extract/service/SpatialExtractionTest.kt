package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCTerms
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class SpatialExtractionTest {
    @Test
    fun extractListOfSpatialReferenceDataCodes() {
        val turtle =
            """
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .

            <https://test.no/service>
                a cpsvno:Service ;
                dct:spatial            <https://data.geonorge.no/administrativeEnheter/fylke/id/34> .

            <https://data.geonorge.no/administrativeEnheter/fylke/id/34>
                a               dct:Location;
                dct:identifier  "34";
                dct:title       "Innlandet" .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expectedSpatial =
            listOf(
                ReferenceDataCode().apply {
                    uri = "https://data.geonorge.no/administrativeEnheter/fylke/id/34"
                    code = "34"
                    prefLabel =
                        LocalizedStrings().apply {
                            no = "Innlandet"
                        }
                },
            )

        val result = subject.extractListOfReferenceDataCodes(DCTerms.spatial, DCTerms.identifier, DCTerms.title)

        assertEquals(expectedSpatial, result)
    }

    @Test
    fun extractOfSpatialIsNullWhenNoRelevantValuesAreExtracted() {
        val turtle =
            """
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .

            <https://test.no/service>
                a cpsvno:Service ;
                dct:spatial [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfReferenceDataCodes(DCTerms.spatial, DCTerms.identifier, DCTerms.title))
    }
}
