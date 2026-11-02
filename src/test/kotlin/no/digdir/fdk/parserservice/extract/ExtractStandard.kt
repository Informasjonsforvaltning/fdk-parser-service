package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.Standard
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class ExtractStandard {
    private fun datasetFrom(turtle: String): Resource {
        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        return m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()
    }

    @Test
    fun extractsAllSeeAlsoLinks() {
        val subject =
            datasetFrom(
                """
                @prefix dct:  <http://purl.org/dc/terms/> .
                @prefix dcat: <http://www.w3.org/ns/dcat#> .
                @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

                <https://testdirektoratet.no/model/dataset/0>
                    a               dcat:Dataset ;
                    dct:conformsTo  [ a             dct:Standard ;
                                      dct:title     "SOSI"@nb ;
                                      rdfs:seeAlso  <https://standard.no/1> , <https://standard.no/2>
                                    ] .
                """.trimIndent(),
            )

        val standard = subject.extractListOfStandards()?.single()

        assertEquals(LocalizedStrings().apply { nb = "SOSI" }, standard?.prefLabel)
        assertEquals(
            listOf("https://standard.no/1", "https://standard.no/2"),
            standard?.seeAlso?.map { it.toString() }?.sorted(),
        )
        assertEquals(standard?.seeAlso?.first(), standard?.uri)
    }

    @Test
    fun fallsBackToSourceThenToTheStandardItself() {
        val subject =
            datasetFrom(
                """
                @prefix dct:  <http://purl.org/dc/terms/> .
                @prefix dcat: <http://www.w3.org/ns/dcat#> .

                <https://testdirektoratet.no/model/dataset/0>
                    a               dcat:Dataset ;
                    dct:conformsTo  [ dct:source <https://source.no> ] ,
                                    <https://standard.no/itself> .
                """.trimIndent(),
            )

        val links =
            subject
                .extractListOfStandards()
                ?.map { it.seeAlso.map { link -> link.toString() } }
                ?.sortedBy { it.first() }

        assertEquals(listOf(listOf("https://source.no"), listOf("https://standard.no/itself")), links)
    }

    @Test
    fun keepsStandardWithOnlyTitle() {
        val subject =
            datasetFrom(
                """
                @prefix dct:  <http://purl.org/dc/terms/> .
                @prefix dcat: <http://www.w3.org/ns/dcat#> .

                <https://testdirektoratet.no/model/dataset/0>
                    a               dcat:Dataset ;
                    dct:conformsTo  [ a dct:Standard ; dct:title "Uten lenke"@nb ] .
                """.trimIndent(),
            )

        val expected =
            Standard().apply {
                prefLabel = LocalizedStrings().apply { nb = "Uten lenke" }
            }

        assertEquals(listOf(expected), subject.extractListOfStandards())
    }

    @Test
    fun returnsNullWhenNoStandards() {
        val subject =
            datasetFrom(
                """
                @prefix dcat: <http://www.w3.org/ns/dcat#> .

                <https://testdirektoratet.no/model/dataset/0> a dcat:Dataset .
                """.trimIndent(),
            )

        assertNull(subject.extractListOfStandards())
    }
}
