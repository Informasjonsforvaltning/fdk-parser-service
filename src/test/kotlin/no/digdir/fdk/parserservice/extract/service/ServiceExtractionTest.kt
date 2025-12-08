package no.digdir.fdk.parserservice.extract.service

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.UriWithLabel
import no.digdir.fdk.model.service.ServiceChannel
import no.digdir.fdk.model.service.ServiceCost
import no.digdir.fdk.model.service.ServiceLegalResource
import no.digdir.fdk.model.service.ServiceOutput
import no.digdir.fdk.model.service.ServiceRequirement
import no.digdir.fdk.model.service.ServiceRule
import no.digdir.fdk.parserservice.vocabulary.CPSV
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class ServiceExtractionTest {
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

        assertEquals(expected, subject.extractListOfServiceChannels())
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

        assertNull(subject.extractListOfServiceChannels())
    }

    @Test
    fun extractListOfServiceRequirements() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:holdsRequirement <https://test.no/requirement/1> .

            <https://test.no/requirement/1>
                a               cv:Requirement ;
                dct:identifier  "1" ;
                dct:title       "Requirement title"@en ;
                dct:description "Requirement description"@en ;
                dct:type        <https://test.no/concept/1> ;
                cv:fulfils      <https://test.no/rule/1> .

            <https://test.no/concept/1>
                a               skos:Concept ;
                dct:source      <https://test.no/source> ;
                skos:prefLabel "Concept"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceRequirement
                    .newBuilder()
                    .setUri("https://test.no/requirement/1")
                    .setIdentifier("1")
                    .setDctTitle(LocalizedStrings().apply { en = "Requirement title" })
                    .setDescription(LocalizedStrings().apply { en = "Requirement description" })
                    .setDctType(
                        listOf(
                            UriWithLabel().apply {
                                uri = "https://test.no/source"
                                prefLabel = LocalizedStrings().apply { en = "Concept" }
                            },
                        ),
                    ).setFulfils(listOf("https://test.no/rule/1"))
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceRequirements())
    }

    @Test
    fun extractListOfServiceCosts() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cv:hasCost      <https://test.no/cost/1> .

            <https://test.no/cost/1>
                a                   cv:Cost ;
                dct:identifier      "1" ;
                dct:description     "Cost description"@en ;
                cv:currency         <http://publications.europa.eu/resource/authority/currency/NOK> ;
                cv:ifAccessedThrough <https://test.no/channel/1> ;
                cv:value            "100.50" .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceCost
                    .newBuilder()
                    .setUri("https://test.no/cost/1")
                    .setIdentifier("1")
                    .setDescription(LocalizedStrings().apply { en = "Cost description" })
                    .setCurrency("http://publications.europa.eu/resource/authority/currency/NOK")
                    .setIfAccessedThrough("https://test.no/channel/1")
                    .setValue("100.50")
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceCosts())
    }

    @Test
    fun extractListOfServiceOutput() {
        val turtle =
            """
            @prefix at:    <http://publications.europa.eu/ontology/authority/> .
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cpsv:produces    <https://test.no/output/1> .

            <https://test.no/output/1>
                a               cv:Output ;
                dct:identifier  "1" ;
                dct:title       "Output name"@en ;
                dct:description "Output description"@en ;
                dct:language    <http://publications.europa.eu/resource/authority/language/ENG> ;
                dct:type        <https://test.no/concept/1> .

            <http://publications.europa.eu/resource/authority/language/ENG>
                a                  skos:Concept;
                at:authority-code  "ENG";
                skos:prefLabel     "English"@en .

            <https://test.no/concept/1>
                a               skos:Concept ;
                skos:prefLabel  "Output type"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceOutput
                    .newBuilder()
                    .setUri("https://test.no/output/1")
                    .setIdentifier("1")
                    .setName(LocalizedStrings().apply { en = "Output name" })
                    .setDescription(LocalizedStrings().apply { en = "Output description" })
                    .setLanguage(
                        listOf(
                            ReferenceDataCode().apply {
                                uri = "http://publications.europa.eu/resource/authority/language/ENG"
                                code = "ENG"
                                prefLabel = LocalizedStrings().apply { en = "English" }
                            },
                        ),
                    ).setType(
                        listOf(
                            ReferenceDataCode().apply {
                                uri = "https://test.no/concept/1"
                                code = "https://test.no/concept/1"
                                prefLabel = LocalizedStrings().apply { en = "Output type" }
                            },
                        ),
                    ).build(),
            )

        assertEquals(expected, subject.extractListOfServiceOutput(CPSV.produces))
    }

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

        val result = subject.extractListOfServiceEvidence()
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
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
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
                cpsv:hasInput   <https://test.no/evidence/1> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val result = subject.extractListOfServiceEvidence()
        assertEquals(1, result?.size)
        assertEquals("https://test.no/evidence/1", result?.first()?.uri)
    }

    @Test
    fun extractListOfServiceLegalResources() {
        val turtle =
            """
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix eli:    <http://data.europa.eu/eli/ontology#> .
            @prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:hasLegalResource <https://test.no/legal/1> .

            <https://test.no/legal/1>
                a               eli:LegalResource ;
                dct:title       "Legal title"@en ;
                dct:description "Legal description"@en ;
                rdfs:seeAlso    <https://test.no/seealso> ;
                dct:relation    <https://test.no/related> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceLegalResource
                    .newBuilder()
                    .setUri("https://test.no/legal/1")
                    .setDctTitle(LocalizedStrings().apply { en = "Legal title" })
                    .setDescription(LocalizedStrings().apply { en = "Legal description" })
                    .setSeeAlso(listOf("https://test.no/seealso"))
                    .setRelation(listOf("https://test.no/related"))
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceLegalResources(CV.hasLegalResource))
    }

    @Test
    fun extractListOfServiceRules() {
        val turtle =
            """
            @prefix at:    <http://publications.europa.eu/ontology/authority/> .
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .

            <https://test.no/service>
                a               cpsvno:Service ;
                cpsv:follows    <https://test.no/rule/1> .

            <https://test.no/rule/1>
                a               cpsv:Rule ;
                dct:identifier  "1" ;
                dct:title       "Rule name"@en ;
                dct:description "Rule description"@en ;
                dct:language    <http://publications.europa.eu/resource/authority/language/ENG> ;
                cpsv:implements <https://test.no/legal/1> .

            <http://publications.europa.eu/resource/authority/language/ENG>
                a                  skos:Concept;
                at:authority-code  "ENG";
                skos:prefLabel     "English"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val expected =
            listOf(
                ServiceRule
                    .newBuilder()
                    .setUri("https://test.no/rule/1")
                    .setIdentifier("1")
                    .setName(LocalizedStrings().apply { en = "Rule name" })
                    .setDescription(LocalizedStrings().apply { en = "Rule description" })
                    .setLanguage(
                        listOf(
                            ReferenceDataCode().apply {
                                uri = "http://publications.europa.eu/resource/authority/language/ENG"
                                code = "ENG"
                                prefLabel = LocalizedStrings().apply { en = "English" }
                            },
                        ),
                    ).setLegalResources(listOf("https://test.no/legal/1"))
                    .build(),
            )

        assertEquals(expected, subject.extractListOfServiceRules())
    }

    @Test
    fun extractListOfServiceContactPoints() {
        val turtle =
            """
            @prefix at:    <http://publications.europa.eu/ontology/authority/> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .
            @prefix vcard:  <http://www.w3.org/2006/vcard/ns#> .

            <https://test.no/service>
                a                   cpsvno:Service ;
                cv:contactPoint  <https://test.no/contact/1> .

            <https://test.no/contact/1>
                a               cv:ContactPoint;
                vcard:category  "Contact type"@en ;
                cv:email        "mailto:test@example.org" ;
                cv:telephone    "tel:+4712345678" ;
                cv:contactPage  <https://test.no/contactpage> ;
                vcard:language  <http://publications.europa.eu/resource/authority/language/ENG> .

            <http://publications.europa.eu/resource/authority/language/ENG>
                a                  skos:Concept;
                at:authority-code  "ENG";
                skos:prefLabel     "English"@en .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.getResource("https://test.no/service")

        val result = subject.extractListOfServiceContactPoints()
        assertEquals(1, result?.size)
        val contact = result?.first()
        assertEquals("https://test.no/contact/1", contact?.uri)
        assertEquals("Contact type", contact?.contactType?.en)
        assertEquals(listOf("+4712345678"), contact?.telephone)
        assertEquals(listOf("test@example.org"), contact?.email)
        assertEquals("https://test.no/contactpage", contact?.contactPage?.first())
    }

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
}
