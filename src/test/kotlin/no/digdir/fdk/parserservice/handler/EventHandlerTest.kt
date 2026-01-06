package no.digdir.fdk.parserservice.handler

import io.kotest.assertions.json.shouldEqualJson
import no.digdir.fdk.parserservice.parser.EventParserRegistry
import no.digdir.fdk.parserservice.parser.event.CpsvApNoV0Parser
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Tag("unit")
class EventHandlerTest {
    private val parserRegistry = EventParserRegistry()
    private val handler = EventHandler(parserRegistry)

    init {
        parserRegistry.registerParser(CpsvApNoV0Parser(), priority = 50, name = "CPSV-AP-NO-V0")
    }

    @Test
    fun parseAndEncodeCpsvnoEvent() {
        val turtle =
            """
            @prefix cpsv: <http://purl.org/vocab/cpsv#> .
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1> a cv:BusinessEvent ;
                dct:identifier "1" ;
                dct:title "Starte og drive restaurant"@nb ;
                dct:description "Elektronisk prosess for etablering og oppstart av en bedrift."@nb ;
                dct:type <https://data.norge.no/vocabulary/event-type#administrative-decision-made> ;
                dct:relation <http://public-service-publisher.fellesdatakatalog.digdir.no/services/1> ;
            .

            <https://data.norge.no/vocabulary/event-type#administrative-decision-made> a skos:Concept ;
                skos:prefLabel  "Vedtak fattet"@nb .

            <https://data.norge.no/concepts/304> a skos:Concept ;
                skos:broader    <https://data.norge.no/concepts/310> ;
                skos:prefLabel  "Drive en bedrift"@nb
            .

            <https://data.norge.no/concepts/310> a skos:Concept ;
                skos:narrower   <https://data.norge.no/concepts/304> ;
                skos:prefLabel  "Starte og drive en bedrift"@nb
            .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/1>
            .
            """.trimIndent()

        val result = handler.parseEvent("fdk-1", turtle)

        val expected = """{
          "id": "fdk-1",
          "uri": "http://public-service-publisher.fellesdatakatalog.digdir.no/events/1",
          "identifier": "1",
          "title": {
            "no": null,
            "nb": "Starte og drive restaurant",
            "nn": null,
            "en": null
          },
          "description": {
            "no": null,
            "nb": "Elektronisk prosess for etablering og oppstart av en bedrift.",
            "nn": null,
            "en": null
          },
          "harvest": {
            "firstHarvested": "2020-10-05T13:15:39.831Z",
            "modified": "2020-10-05T13:15:39.831Z"
          },
          "catalog": null,
          "dctType": [
            {
              "uri": "https://data.norge.no/vocabulary/event-type#administrative-decision-made",
              "code": "administrative-decision-made",
              "prefLabel": {
                "no": null,
                "nb": "Vedtak fattet",
                "nn": null,
                "en": null
              }
            }
          ],
          "relation": [
            "http://public-service-publisher.fellesdatakatalog.digdir.no/services/1"
          ],
          "mayInitiate": null,
          "subject": null,
          "distribution": null,
          "specializedType": "business_event"
        }"""

        result.toString().shouldEqualJson(expected)
    }

    @Test
    fun parseLifeEvent() {
        val turtle =
            """
            @prefix cpsv: <http://purl.org/vocab/cpsv#> .
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix cv: <http://data.europa.eu/m8g/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/events/2> a cv:LifeEvent ;
                dct:identifier "2" ;
                dct:title "Oppgjør etter dødsfall"@nb ;
                dct:description "Elektronisk prosess for oppgjør etter dødsfall."@nb ;
                dct:type <https://data.norge.no/vocabulary/event-type#administrative-decision-made> ;
                dct:relation <http://public-service-publisher.fellesdatakatalog.digdir.no/services/1> ;
            .

            <https://data.norge.no/vocabulary/event-type#administrative-decision-made> a skos:Concept ;
                skos:prefLabel  "Vedtak fattet"@nb .

            <http://localhost:5000/events/fdk-2>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-2" ;
                    dct:issued         "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    dct:modified       "2020-10-05T13:15:39.831Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <http://public-service-publisher.fellesdatakatalog.digdir.no/events/2>
            .
            """.trimIndent()

        val result = handler.parseEvent("fdk-2", turtle)

        val expected = """{
          "id": "fdk-2",
          "uri": "http://public-service-publisher.fellesdatakatalog.digdir.no/events/2",
          "identifier": "2",
          "title": {
            "no": null,
            "nb": "Oppgjør etter dødsfall",
            "nn": null,
            "en": null
          },
          "description": {
            "no": null,
            "nb": "Elektronisk prosess for oppgjør etter dødsfall.",
            "nn": null,
            "en": null
          },
          "harvest": {
            "firstHarvested": "2020-10-05T13:15:39.831Z",
            "modified": "2020-10-05T13:15:39.831Z"
          },
          "catalog": null,
          "dctType": [
            {
              "uri": "https://data.norge.no/vocabulary/event-type#administrative-decision-made",
              "code": "administrative-decision-made",
              "prefLabel": {
                "no": null,
                "nb": "Vedtak fattet",
                "nn": null,
                "en": null
              }
            }
          ],
          "relation": [
            "http://public-service-publisher.fellesdatakatalog.digdir.no/services/1"
          ],
          "mayInitiate": null,
          "subject": null,
          "distribution": null,
          "specializedType": "life_event"
        }"""

        result.toString().shouldEqualJson(expected)
    }

    @Test
    fun exceptionWhenCatalogRecordHasNoPrimaryTopic() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://localhost:5000/events/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" .
            """.trimIndent()

        assertThrows<Exception> {
            handler.parseEvent("fdk-1", turtle)
        }
    }
}
