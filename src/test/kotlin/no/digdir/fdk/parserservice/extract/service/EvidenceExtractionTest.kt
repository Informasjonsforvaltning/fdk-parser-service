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
    fun extractRequiredEvidenceWithDatasetTypeAndValidityDuration() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .
            @prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .

            <https://test.no/service>
                a                           cpsvno:Service ;
                cpsvno:hasRequiredEvidence  <https://test.no/evidence/1> .

            <https://test.no/evidence/1>
                a                       cpsvno:RequiredEvidence ;
                dct:identifier          "1" ;
                dct:isPartOf            <https://test.no/dataset/1> ,
                                        <https://test.no/dataset/2> ;
                dct:type                <https://data.norge.no/vocabulary/evidence-type#attestation> ;
                cpsvno:validityDuration "P6M"^^xsd:duration .

            <https://data.norge.no/vocabulary/evidence-type#attestation>
                a               skos:Concept ;
                skos:prefLabel  "Attest"@nb .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val result = subject.extractListOfServiceEvidence(CPSVNO.hasRequiredEvidence)
        assertEquals(1, result?.size)
        val evidence = result!!.first()

        assertEquals("https://data.norge.no/vocabulary/cpsvno#RequiredEvidence", evidence.rdfType?.toString())
        assertEquals(
            listOf("https://test.no/dataset/1", "https://test.no/dataset/2"),
            evidence.isPartOf?.map { it.toString() }?.sorted(),
        )
        assertEquals("P6M", evidence.validityDuration?.toString())

        assertEquals(1, evidence.dctType?.size)
        val type = evidence.dctType!!.first()
        assertEquals("https://data.norge.no/vocabulary/evidence-type#attestation", type.uri?.toString())
        assertEquals("attestation", type.code?.toString())
        assertEquals("Attest", type.prefLabel?.nb?.toString())
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
