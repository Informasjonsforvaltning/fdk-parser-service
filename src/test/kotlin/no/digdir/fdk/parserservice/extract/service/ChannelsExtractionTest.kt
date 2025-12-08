package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.service.ServiceChannel
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader

@Tag("unit")
class ChannelsExtractionTest {
    @Test
    fun extractListOfServiceChannels() {
        val turtle =
            """
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .
            @prefix vcard:  <http://www.w3.org/2006/vcard/ns#> .
            @prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cv:hasChannel   <https://test.no/channel/1> .

            <https://test.no/channel/1>
                a                   cv:Channel ;
                dct:identifier      "1" ;
                dct:type            <https://data.norge.no/vocabulary/service-channel-type#assistant> ;
                dct:description     "Channel description"@en ;
                cv:processingTime   "P1D"^^xsd:duration ;
                cpsv:hasInput       <https://test.no/evidence/1> ;
                vcard:hasEmail      "mailto:test@example.org" ;
                vcard:hasURL        <https://test.no/contact> ;
                vcard:hasTelephone  "tel:+4712345678" .

            <https://data.norge.no/vocabulary/service-channel-type#assistant>
                a               skos:Concept;
                skos:prefLabel  "assistant"@en , "assistent"@nb .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceChannel
                    .newBuilder()
                    .setUri("https://test.no/channel/1")
                    .setIdentifier("1")
                    .setChannelType(
                        ReferenceDataCode().apply {
                            uri = "https://data.norge.no/vocabulary/service-channel-type#assistant"
                            code = "assistant"
                            prefLabel =
                                LocalizedStrings().apply {
                                    en = "assistant"
                                    nb = "assistent"
                                }
                        },
                    ).setDescription(LocalizedStrings().apply { en = "Channel description" })
                    .setProcessingTime("P1D")
                    .setHasInput(listOf("https://test.no/evidence/1"))
                    .setEmail(listOf("mailto:test@example.org"))
                    .setUrl(listOf("https://test.no/contact"))
                    .setTelephone(listOf("tel:+4712345678"))
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceChannelsV0())
    }

    @Test
    fun extractListOfServiceChannelsReturnsNullWhenNoChannels() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .

            <https://test.no/service>
                a               cpsvno:Service .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfServiceChannelsV0())
    }

    @Test
    fun extractListOfServiceChannelsV1() {
        val turtle =
            """
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .
            @prefix vcard:  <http://www.w3.org/2006/vcard/ns#> .
            @prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cv:hasChannel   <https://test.no/channel/1> .

            <https://test.no/channel/1>
                a                   cv:Channel ;
                dct:identifier      "1" ;
                dct:type            <https://data.norge.no/vocabulary/service-channel-type#assistant> ;
                dct:description     "Channel description"@en ;
                cv:processingTime   "P1D"^^xsd:duration ;
                cpsvno:hasRequiredEvidence   <https://test.no/evidence/1> ;
                vcard:hasEmail      "mailto:test@example.org" ;
                vcard:hasURL        <https://test.no/contact> ;
                vcard:hasTelephone  "tel:+4712345678" .

            <https://data.norge.no/vocabulary/service-channel-type#assistant>
                skos:prefLabel  "assistant"@en , "assistent"@nb .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceChannel
                    .newBuilder()
                    .setUri("https://test.no/channel/1")
                    .setIdentifier("1")
                    .setChannelType(
                        ReferenceDataCode().apply {
                            uri = "https://data.norge.no/vocabulary/service-channel-type#assistant"
                            code = "assistant"
                            prefLabel =
                                LocalizedStrings().apply {
                                    en = "assistant"
                                    nb = "assistent"
                                }
                        },
                    ).setDescription(LocalizedStrings().apply { en = "Channel description" })
                    .setProcessingTime("P1D")
                    .setHasInput(listOf("https://test.no/evidence/1"))
                    .setEmail(listOf("mailto:test@example.org"))
                    .setUrl(listOf("https://test.no/contact"))
                    .setTelephone(listOf("tel:+4712345678"))
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceChannelsV1())
    }

    @Test
    fun extractOfServiceChannelsIsNullWhenNoRelevantValuesAreExtracted() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cv:hasChannel   [ ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        assertNull(subject.extractListOfServiceChannelsV1())
    }
}
