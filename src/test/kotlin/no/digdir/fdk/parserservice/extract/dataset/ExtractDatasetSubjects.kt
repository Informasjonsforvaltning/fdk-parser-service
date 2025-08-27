package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.dataset.Subject
import no.digdir.fdk.parseservice.extract.dataset.extractListOfSubjects
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractDatasetSubjects {

    @Test
    fun extractListOfSubjects() {
        val turtle = """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset ;
                dct:subject     [  a                skos:Concept ;
                                   dct:identifier   "http://begrepskatalogen/begrep/123" ;
                                   skos:definition  "Definisjon"@nb ;
                                   skos:prefLabel   "dokument"@nb
                                ] ,
                                <https://testdirektoratet.no/model/concept> .

            <https://testdirektoratet.no/model/concept>
                a                skos:Concept ;
                dct:identifier   "http://begrepskatalogen/begrep/321" ;
                skos:definition  "er lei seg fordi noko ikkje vart slik ein venta eller vona"@nn ;
                skos:prefLabel   "vonbroten"@nn .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = listOf(
            Subject().apply {
                uri = "https://testdirektoratet.no/model/concept"
                identifier = "http://begrepskatalogen/begrep/321"
                prefLabel = LocalizedStrings().apply { nn = "vonbroten" }
                definition = LocalizedStrings().apply { nn = "er lei seg fordi noko ikkje vart slik ein venta eller vona" }
            },
            Subject().apply {
                identifier = "http://begrepskatalogen/begrep/123"
                prefLabel = LocalizedStrings().apply { nb = "dokument" }
                definition = LocalizedStrings().apply { nb = "Definisjon" }
            }
        )

        assertEquals(expected, subject.extractListOfSubjects())
    }
}