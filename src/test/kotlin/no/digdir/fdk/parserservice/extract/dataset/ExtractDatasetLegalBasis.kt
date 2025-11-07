package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.UriWithLabelAndType
import no.digdir.fdk.parserservice.vocabulary.CPSVNO
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractDatasetLegalBasis {
    @Test
    fun extractLegalBasisForProcessing() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
            @prefix cpsv: <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix eli: <http://data.europa.eu/eli/ontology#> .

                <https://testdirektoratet.no/model/dataset/0>
                    a               dcat:Dataset ;
                    cpsv:follows  <https://testdirektoratet.no/legal-basis> .

                <https://testdirektoratet.no/legal-basis>
                    a cpsv:Rule ;
                    dct:type cpsvno:ruleForDataProcessing ;
                    cpsv:implements [ a eli:LegalResouce ;
                        rdfs:seeAlso "https://lovdata.no/NL/lov/2016-12-09-88/§1-1" ;
                        dct:type    [ a skos:Concept ;
                            skos:prefLabel  "Folkeregisterloven §1-1"@nb ;
                        ]
                    ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected =
            listOf(
                UriWithLabelAndType().apply {
                    uri = "https://lovdata.no/NL/lov/2016-12-09-88/§1-1"
                    extraType = CPSVNO.ruleForDataProcessing.uri
                    prefLabel = LocalizedStrings().apply { nb = "Folkeregisterloven §1-1" }
                },
            )

        assertEquals(expected, subject.extractListOfLegalBasisV2(CPSVNO.ruleForDataProcessing))
    }

    @Test
    fun extractLegalBasisForRestriction() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
            @prefix cpsv: <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix eli: <http://data.europa.eu/eli/ontology#> .

                <https://testdirektoratet.no/model/dataset/0>
                    a               dcat:Dataset ;
                    cpsv:follows  <https://testdirektoratet.no/legal-basis> .

                <https://testdirektoratet.no/legal-basis>
                    a cpsv:Rule ;
                    dct:type cpsvno:ruleForNonDisclosure ;
                    cpsv:implements [ a eli:LegalResouce ;
                        rdfs:seeAlso <https://lovdata.no/NL/lov/2016-05-27-14/§3-1> ;
                    ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected =
            listOf(
                UriWithLabelAndType().apply {
                    uri = "https://lovdata.no/NL/lov/2016-05-27-14/§3-1"
                    extraType = CPSVNO.ruleForNonDisclosure.uri
                },
            )

        assertEquals(expected, subject.extractListOfLegalBasisV2(CPSVNO.ruleForNonDisclosure))
    }

    @Test
    fun extractLegalBasisForAccess() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
            @prefix cpsv: <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix eli: <http://data.europa.eu/eli/ontology#> .

                <https://testdirektoratet.no/model/dataset/0>
                    a               dcat:Dataset ;
                    cpsv:follows  <https://testdirektoratet.no/legal-basis> .

                <https://testdirektoratet.no/legal-basis>
                    a cpsv:Rule ;
                    dct:type cpsvno:ruleForDisclosure ;
                    cpsv:implements [ a eli:LegalResouce ;
                        dct:type    [ a skos:Concept ;
                            skos:prefLabel  "Valutaregisterloven §9"@nb ;
                        ]
                    ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected =
            listOf(
                UriWithLabelAndType().apply {
                    extraType = CPSVNO.ruleForDisclosure.uri
                    prefLabel = LocalizedStrings().apply { nb = "Valutaregisterloven §9" }
                },
            )

        assertEquals(expected, subject.extractListOfLegalBasisV2(CPSVNO.ruleForDisclosure))
    }

    @Test
    fun extractLegalBasisIsIgnoredWhenMissingBothUriAndLabel() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
            @prefix cpsv: <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix eli: <http://data.europa.eu/eli/ontology#> .

                <https://testdirektoratet.no/model/dataset/0>
                    a               dcat:Dataset ;
                    cpsv:follows  <https://testdirektoratet.no/legal-basis> .

                <https://testdirektoratet.no/legal-basis>
                    a cpsv:Rule ;
                    dct:type cpsvno:ruleForDisclosure .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        assertNull(subject.extractListOfLegalBasisV2(CPSVNO.ruleForDisclosure))
    }
}
