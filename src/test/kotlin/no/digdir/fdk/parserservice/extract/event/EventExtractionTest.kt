package no.digdir.fdk.parserservice.extract.event

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.parserservice.parser.event.CpsvApNoV0Parser
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class EventExtractionTest {
    private val eventIRI = "http://public-service-publisher.fellesdatakatalog.digdir.no/events/1"
    private val fdkId = "fdk-1"
    private val parser = CpsvApNoV0Parser()

    @Test
    fun extractsTitleAndDescriptionCorrectly() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:BusinessEvent ;
                dct:title "title nb"@nb , "title nn"@nn , "title en"@en , "title no"@no ;
                dct:description "description nb"@nb , "description nn"@nn , "description en"@en , "description no"@no .
            """.trimIndent()

        val expectedTitle =
            LocalizedStrings().also { title ->
                title.nb = "title nb"
                title.nn = "title nn"
                title.en = "title en"
                title.no = "title no"
            }

        val expectedDescription =
            LocalizedStrings().also { description ->
                description.nb = "description nb"
                description.nn = "description nn"
                description.en = "description en"
                description.no = "description no"
            }

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, eventIRI, fdkId)

        assertEquals(expectedTitle, result.title)
        assertEquals(expectedDescription, result.description)
    }

    @Test
    fun extractIdentifier() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:BusinessEvent ;
                dct:identifier "event-identifier-123" .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, eventIRI, fdkId)

        assertEquals("event-identifier-123", result.identifier)
    }

    @Test
    fun extractDctType() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:BusinessEvent ;
                dct:type <https://data.norge.no/vocabulary/event-type#administrative-decision-made> .

            <https://data.norge.no/vocabulary/event-type#administrative-decision-made> a skos:Concept ;
                skos:prefLabel  "Vedtak fattet"@nb .
            """.trimIndent()

        val expectedDctType =
            listOf(
                ReferenceDataCode().apply {
                    uri = "https://data.norge.no/vocabulary/event-type#administrative-decision-made"
                    code = "administrative-decision-made"
                    prefLabel =
                        LocalizedStrings().apply {
                            nb = "Vedtak fattet"
                        }
                },
            )

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, eventIRI, fdkId)

        assertEquals(expectedDctType, result.dctType)
    }

    @Test
    fun extractRelation() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:BusinessEvent ;
                dct:relation <http://public-service-publisher.fellesdatakatalog.digdir.no/services/1> ,
                            <http://public-service-publisher.fellesdatakatalog.digdir.no/services/2> .
            """.trimIndent()

        val expectedRelation =
            listOf(
                "http://public-service-publisher.fellesdatakatalog.digdir.no/services/1",
                "http://public-service-publisher.fellesdatakatalog.digdir.no/services/2",
            )

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, eventIRI, fdkId)

        assertEquals(expectedRelation, result.relation?.map { it.toString() }?.sorted())
    }

    @Test
    fun extractMayInitiate() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:BusinessEvent ;
                cpsvno:mayInitiate <http://public-service-publisher.fellesdatakatalog.digdir.no/services/1> ,
                                   <http://public-service-publisher.fellesdatakatalog.digdir.no/services/2> .
            """.trimIndent()

        val expectedMayInitiate =
            listOf(
                "http://public-service-publisher.fellesdatakatalog.digdir.no/services/1",
                "http://public-service-publisher.fellesdatakatalog.digdir.no/services/2",
            )

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, eventIRI, fdkId)

        assertEquals(expectedMayInitiate, result.mayInitiate?.map { it.toString() }?.sorted())
    }

    @Test
    fun extractSubject() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:BusinessEvent ;
                dct:subject <https://data.norge.no/subject/1> ,
                          <https://data.norge.no/subject/2> .
            """.trimIndent()

        val expectedSubject =
            listOf(
                "https://data.norge.no/subject/1",
                "https://data.norge.no/subject/2",
            )

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, eventIRI, fdkId)

        assertEquals(expectedSubject, result.subject?.map { it.toString() }?.sorted())
    }

    @Test
    fun extractDistribution() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:BusinessEvent ;
                dcat:distribution <https://data.norge.no/distribution/1> ,
                                 <https://data.norge.no/distribution/2> .
            """.trimIndent()

        val expectedDistribution =
            listOf(
                "https://data.norge.no/distribution/1",
                "https://data.norge.no/distribution/2",
            )

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, eventIRI, fdkId)

        assertEquals(expectedDistribution, result.distribution?.map { it.toString() }?.sorted())
    }

    @Test
    fun extractSpecializedTypeBusinessEvent() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:BusinessEvent .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, eventIRI, fdkId)

        assertEquals("business_event", result.specializedType)
    }

    @Test
    fun extractSpecializedTypeLifeEvent() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:LifeEvent .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, eventIRI, fdkId)

        assertEquals("life_event", result.specializedType)
    }

    @Test
    fun extractHarvestMetadata() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:BusinessEvent .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, eventIRI, fdkId)

        assertEquals("2020-10-05T13:15:39.831Z", result.harvest?.firstHarvested)
        assertEquals("2020-10-05T13:15:39.831Z", result.harvest?.modified)
    }
}
