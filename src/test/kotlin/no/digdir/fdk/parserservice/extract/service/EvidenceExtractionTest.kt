package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.parserservice.vocabulary.CPSV
import no.digdir.fdk.parserservice.vocabulary.CPSVNO
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader

@Tag("unit")
class EvidenceExtractionTest {
    @Test
    fun extractListOfServiceEvidence() {
        val turtle =
            """
            @prefix at:    <http://publications.europa.eu/ontology/authority/> .
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix foaf:   <http://xmlns.com/foaf/0.1/> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cpsv:hasInput   <https://test.no/evidence/1> ,
                                <https://test.no/evidence/2> ;
                cv:hasChannel  <https://test.no/channel/1> .

            <https://test.no/evidence/1>
                a               cv:Evidence ;
                dct:identifier  "1" ;
                dct:title       "Evidence name"@en ;
                dct:description "Evidence description"@en ;
                dct:language    <http://publications.europa.eu/resource/authority/language/ENG> ;
                dct:type        <https://test.no/concept/1> ;
                foaf:page       <https://test.no/page> .

            <https://test.no/evidence/2>
                a               dcat:Dataset ;
                dct:identifier  "2" ;
                dct:title       "Dataset evidence"@en .

            <https://test.no/evidence/3>
                a               cv:Evidence ;
                dct:identifier  "3" ;
                dct:title       "Channel evidence"@en .

            <https://test.no/channel/1>
                a               cv:Channel ;
                cpsv:hasInput   <https://test.no/evidence/3> ,
                                <https://test.no/evidence/1> .

            <http://publications.europa.eu/resource/authority/language/ENG>
                a                  skos:Concept;
                at:authority-code  "ENG";
                skos:prefLabel     "English"@en .

            <https://test.no/concept/1>
                a               skos:Concept ;
                skos:prefLabel  "Evidence type"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val result = subject.extractListOfServiceEvidence(CPSV.hasInput)
        assertEquals(3, result?.size)
        val uris = result?.map { it.uri as String }?.sorted()
        assertEquals(
            listOf("https://test.no/evidence/1", "https://test.no/evidence/2", "https://test.no/evidence/3"),
            uris,
        )
    }

    @Test
    fun extractListOfServiceEvidenceFromChannelsOnly() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cv:hasChannel  <https://test.no/channel/1> .

            <https://test.no/evidence/1>
                a               cv:Evidence ;
                dct:identifier  "1" ;
                dct:title       "Channel evidence"@en .

            <https://test.no/channel/1>
                a               cv:Channel ;
                cpsvno:hasRequiredEvidence   <https://test.no/evidence/1> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val result = subject.extractListOfServiceEvidence(CPSVNO.hasRequiredEvidence)
        assertEquals(1, result?.size)
        assertEquals("https://test.no/evidence/1", result?.first()?.uri)
    }

    @Test
    fun extractOfServiceEvidenceIsNullWhenNoRelevantValuesAreExtracted() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cv:hasChannel  [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfServiceEvidence(CPSVNO.hasRequiredEvidence))
    }
}
