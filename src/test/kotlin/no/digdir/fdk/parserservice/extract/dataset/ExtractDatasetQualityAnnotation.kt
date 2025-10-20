package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.dataset.QualityAnnotation
import no.digdir.fdk.parserservice.vocabulary.DQVISO
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractDatasetQualityAnnotation {

    @Test
    fun extractRelevanceAnnotation() {
        val turtle = """
        @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        @prefix dqv:   <http://www.w3.org/ns/dqv#> .
        @prefix dcat:  <http://www.w3.org/ns/dcat#> .
        @prefix dct: <http://purl.org/dc/terms/> .
        @prefix oa:    <http://www.w3.org/ns/oa#> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset ;
                dqv:hasQualityAnnotation  <https://testdirektoratet.no/relevance> .

            <https://testdirektoratet.no/relevance>
                a                dqv:QualityAnnotation ;
                dqv:inDimension  <http://iso.org/25012/2008/dataquality/Relevance> ;
                oa:hasBody       [
                    a               oa:TextualBody ;
                    rdf:value       "relevans" ;
                    dct:language     <http://publications.europa.eu/resource/authority/language/NNO>
                ] .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = QualityAnnotation().apply {
            inDimension = DQVISO.Relevance.uri
            hasBody = LocalizedStrings().apply { nn = "relevans" }
        }

        assertEquals(expected, subject.extractQualityAnnotation(DQVISO.Relevance))
    }

    @Test
    fun extractCurrentnessAnnotation() {
        val turtle = """
        @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        @prefix dqv:   <http://www.w3.org/ns/dqv#> .
        @prefix dcat:  <http://www.w3.org/ns/dcat#> .
        @prefix dct: <http://purl.org/dc/terms/> .
        @prefix oa:    <http://www.w3.org/ns/oa#> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset ;
                dqv:hasQualityAnnotation  <https://testdirektoratet.no/currentness> .

            <https://testdirektoratet.no/currentness>
                a                dqv:QualityAnnotation ;
                dqv:inDimension
                    <http://iso.org/25012/2008/dataquality/Currentness> ;
                oa:hasBody     [
                    a               oa:TextualBody ;
                    rdf:value       "currentness" ;
                    dct:language     <http://publications.europa.eu/resource/authority/language/ENG>
                ] .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = QualityAnnotation().apply {
            inDimension = DQVISO.Currentness.uri
            hasBody = LocalizedStrings().apply { en = "currentness" }
        }

        assertEquals(expected, subject.extractQualityAnnotation(DQVISO.Currentness))
    }

    @Test
    fun extractAvailabilityAnnotation() {
        val turtle = """
        @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        @prefix dqv:   <http://www.w3.org/ns/dqv#> .
        @prefix dcat:  <http://www.w3.org/ns/dcat#> .
        @prefix dct: <http://purl.org/dc/terms/> .
        @prefix oa:    <http://www.w3.org/ns/oa#> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset ;
                dqv:hasQualityAnnotation  <https://testdirektoratet.no/availability> .

            <https://testdirektoratet.no/availability>
                a                dqv:QualityAnnotation ;
                dqv:inDimension
                    <http://iso.org/25012/2008/dataquality/Availability> ;
                oa:hasBody     [
                    a    oa:TextualBody ;
                    rdf:value  "availability"
                ] .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = QualityAnnotation().apply {
            inDimension = DQVISO.Availability.uri
            hasBody = LocalizedStrings().apply { no = "availability" }
        }

        assertEquals(expected, subject.extractQualityAnnotation(DQVISO.Availability))
    }

    @Test
    fun extractAccuracyAnnotation() {
        val turtle = """
        @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        @prefix dqv:   <http://www.w3.org/ns/dqv#> .
        @prefix dcat:  <http://www.w3.org/ns/dcat#> .
        @prefix dct: <http://purl.org/dc/terms/> .
        @prefix oa:    <http://www.w3.org/ns/oa#> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset ;
                dqv:hasQualityAnnotation  <https://testdirektoratet.no/accuracy> .

            <https://testdirektoratet.no/accuracy>
                a                dqv:QualityAnnotation ;
                dqv:inDimension
                    <http://iso.org/25012/2008/dataquality/Accuracy> ;
                oa:hasBody     [ a    oa:TextualBody ;
                    rdf:value  "Accuracy" ;
                    dct:language     <http://publications.europa.eu/resource/authority/language/ENG> ;
                    dct:format       <http://publications.europa.eu/resource/authority/file-type/TXT> ] .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = QualityAnnotation().apply {
            inDimension = DQVISO.Accuracy.uri
            hasBody = LocalizedStrings().apply { en = "Accuracy" }
        }

        assertEquals(expected, subject.extractQualityAnnotation(DQVISO.Accuracy))
    }

    @Test
    fun extractCompletenessAnnotation() {
        val turtle = """
        @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        @prefix dqv:   <http://www.w3.org/ns/dqv#> .
        @prefix dcat:  <http://www.w3.org/ns/dcat#> .
        @prefix dct: <http://purl.org/dc/terms/> .
        @prefix oa:    <http://www.w3.org/ns/oa#> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset ;
                dqv:hasQualityAnnotation  <https://testdirektoratet.no/completeness> .

            <https://testdirektoratet.no/completeness>
                a                dqv:QualityAnnotation ;
                dqv:inDimension  <http://iso.org/25012/2008/dataquality/Completeness> ;
                oa:hasBody     [
                    rdf:value  "Completeness" ;
                    dct:language     <http://publications.europa.eu/resource/authority/language/NOB> ;
                    dct:format       <http://publications.europa.eu/resource/authority/file-type/TXT> ] .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = QualityAnnotation().apply {
            inDimension = DQVISO.Completeness.uri
            hasBody = LocalizedStrings().apply { nb = "Completeness" }
        }

        assertEquals(expected, subject.extractQualityAnnotation(DQVISO.Completeness))
    }

    @Test
    fun extractIgnoresAnnotationWithNoBody() {
        val turtle = """
        @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        @prefix dqv:   <http://www.w3.org/ns/dqv#> .
        @prefix dcat:  <http://www.w3.org/ns/dcat#> .
        @prefix dct: <http://purl.org/dc/terms/> .
        @prefix oa:    <http://www.w3.org/ns/oa#> .

            <https://testdirektoratet.no/model/dataset/0>
                a               dcat:Dataset ;
                dqv:hasQualityAnnotation  <https://testdirektoratet.no/completeness> .

            <https://testdirektoratet.no/completeness>
                a                dqv:QualityAnnotation ;
                dqv:inDimension  <http://iso.org/25012/2008/dataquality/Completeness> .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        assertNull(subject.extractQualityAnnotation(DQVISO.Completeness))
    }
}
