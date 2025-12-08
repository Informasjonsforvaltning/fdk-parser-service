package no.digdir.fdk.parserservice.extract.service

import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ParticipatingAgentsExtractionTest {
    @Test
    fun extractListOfParticipatingAgents() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:hasParticipation <https://test.no/participation/1> .

            <https://test.no/participation/1>
                a                   cv:Participation ;
                dct:identifier      "1" ;
                dct:description     "Participation description"@en ;
                cv:role             <https://test.no/role/1> ;
                cv:hasParticipant   <https://test.no/agent/1> .

            <https://test.no/agent/1>
                a               dct:Agent ;
                dct:identifier  "1" ;
                dct:description "Agent name"@en .

            <https://test.no/role/1>
                a               skos:Concept ;
                dct:identifier  "data-consumer" ;
                skos:prefLabel  "Data consumer"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val result = subject.extractListOfParticipatingAgents()
        assertEquals(1, result?.size)
        assertEquals("https://test.no/agent/1", result?.first()?.uri)
    }

    @Test
    fun extractOfParticipatingAgentsIsNullWhenNoRelevantValuesAreExtracted() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:hasParticipation <https://test.no/participation/1> .

            <https://test.no/participation/1>
                a                   cv:Participation .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfParticipatingAgents())
    }
}
