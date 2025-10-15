package no.digdir.fdk.parserservice.handler

import io.kotest.assertions.json.shouldEqualJson
import no.digdir.fdk.parseservice.parser.dataset.DcatApNoV1Parser
import no.digdir.fdk.parseservice.handler.DatasetHandler
import no.digdir.fdk.parseservice.parser.dataset.DcatApNoV2Parser
import no.digdir.fdk.parseservice.parser.dataset.MobilityDcatApV3Parser
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Tag("unit")
class DatasetHandlerTest {
    val handler = DatasetHandler(DcatApNoV1Parser(), DcatApNoV2Parser(), MobilityDcatApV3Parser())

    @Test
    fun parseAndEncodeDataset() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix schema:  <http://schema.org/> .
            @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
                
            <https://testdirektoratet.no/model/distribution>
                a                dcat:Distribution ;
                dct:title        "Distribution"@en ;
                dct:format        "csv" ;
                dct:format        <https://www.iana.org/assignments/media-types/text/csv> ;
                dcat:accessURL   <https://testdirektoratet.no/access> ;
                dcat:downloadURL <https://testdirektoratet.no/download> .

            <https://www.iana.org/assignments/media-types/text/csv>
                a               dct:MediaType ;
                dct:identifier  "text/csv" ;
                dct:title       "csv" .
            
            <https://testdirektoratet.no/model/catalog>
                a               dcat:Catalog ;
                dct:identifier  <https://id.no> ;
                dcat:dataset    <https://testdirektoratet.no/model/dataset/0> ;
                dct:title       "Katalog"@no ;
                dct:description "Beskrivelse av katalog"@no ;
                dct:publisher   <https://testdirektoratet.no/publisher> .

            <https://testdirektoratet.no/publisher>
                a               foaf:Agent ;
                dct:identifier  "112233445" .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset ;
                dct:title                 "title"@nb ;
                dct:description           "description"@nb ;
                dct:issued         "2018-01-11T10:50:10.111Z"^^xsd:dateTime ;
                dct:modified       "2020-02-22T12:52:20.222Z"^^xsd:dateTime ;
                dct:temporal    [ a                   dct:PeriodOfTime ;
                                  schema:startDate    "2019-04-02"^^xsd:date ;
                                  schema:endDate      "2020-04-02"^^xsd:date ] ;
                dcat:contactPoint [ vcard:hasEmail    "post@mail.com" ];
                dcat:distribution   <https://testdirektoratet.no/model/distribution> .
        """.trimIndent()

        val expected = """{
          "id": "a1c680ca-62d7-34d5-aa4c-d39b5db033ae",
          "uri": "https://testdirektoratet.no/model/dataset/0",
          "identifier": null,
          "admsIdentifier": null,
          "harvest": null,
          "catalog": {
            "id": "https://id.no",
            "uri": "https://testdirektoratet.no/model/catalog",
            "publisher": {
              "id": "112233445",
              "uri": "https://testdirektoratet.no/publisher",
              "name": null,
              "orgPath": null,
              "prefLabel": null,
              "organisasjonsform": null
            },
            "title": {
              "no": "Katalog",
              "nb": null,
              "nn": null,
              "en": null
            },
            "description": {
              "no": "Beskrivelse av katalog",
              "nb": null,
              "nn": null,
              "en": null
            }
          },
          "title": {
            "no": null,
            "nb": "title",
            "nn": null,
            "en": null
          },
          "description": {
            "no": null,
            "nb": "description",
            "nn": null,
            "en": null
          },
          "descriptionFormatted": {
            "no": null,
            "nb": "description",
            "nn": null,
            "en": null
          },
          "publisher": null,
          "distribution": [
            {
              "uri": "https://testdirektoratet.no/model/distribution",
              "title": {
                "no": null,
                "nb": null,
                "nn": null,
                "en": "Distribution"
              },
              "description": null,
              "accessURL": [
                "https://testdirektoratet.no/access"
              ],
              "downloadURL": [
                "https://testdirektoratet.no/download"
              ],
              "license": null,
              "conformsTo": null,
              "page": null,
              "fdkFormat": [
                {
                  "uri": "https://www.iana.org/assignments/media-types/text/csv",
                  "name": "csv",
                  "code": "text/csv",
                  "type": "MEDIA_TYPE"
                },
                {
                  "uri": null,
                  "name": "csv",
                  "code": "csv",
                  "type": "UNKNOWN"
                }
              ],
              "compressFormat": null,
              "packageFormat": null,
              "accessService": null
            }
          ],
          "sample": null,
          "contactPoint": [
            {
                  "uri": null,
                  "email": "post@mail.com",
                  "fullname": null,
                  "hasURL": null,
                  "hasTelephone": null,
                  "organizationName": null,
                  "organizationUnit": null
            }
          ],
          "themeUris": null,
          "theme": null,
          "losTheme": null,
          "eurovocThemes": null,
          "keyword": null,
          "issued": "2018-01-11T10:50:10.111Z",
          "modified": "2020-02-22T12:52:20.222Z",
          "dctType": null,
          "accessRights": null,
          "language": null,
          "page": null,
          "landingPage": null,
          "temporal": [
            {
              "uri": null,
              "startDate": "2019-04-02",
              "endDate": "2020-04-02"
            }
          ],
          "subject": null,
          "spatial": null,
          "provenance": null,
          "accrualPeriodicity": null,
          "legalBasisForRestriction": null,
          "legalBasisForProcessing": null,
          "legalBasisForAccess": null,
          "conformsTo": null,
          "references": null,
          "hasAccuracyAnnotation": null,
          "hasCompletenessAnnotation": null,
          "hasCurrentnessAnnotation": null,
          "hasAvailabilityAnnotation": null,
          "hasRelevanceAnnotation": null,
          "qualifiedAttributions": null,
          "isOpenData": false,
          "isAuthoritative": false,
          "isRelatedToTransportportal": false,
          "inSeries": null,
          "prev": null,
          "last": null,
          "datasetsInSeries": null,
          "type": "datasets",
          "specializedType": null
        }""".trimIndent()

        val result = handler.parseDataset("a1c680ca-62d7-34d5-aa4c-d39b5db033ae", turtle)
        result.toString().shouldEqualJson(expected)
    }

    @Test
    fun exceptionWhenCatalogRecordHasNoPrimaryRecord() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" .
        """.trimIndent()

        assertThrows<Exception> { handler.parseDataset("a1c680ca-62d7-34d5-aa4c-d39b5db033ae", turtle) }
    }

    @Test
    fun parsePrioritizeV2() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix schema:  <http://schema.org/> .
                
            <https://testdirektoratet.no/model/distribution>
                a                dcat:Distribution ;
                dct:title        "Distribution"@en ;
                dcat:mediaType   <https://www.iana.org/assignments/media-types/text/csv> ;
                dcat:accessURL   <https://testdirektoratet.no/access> ;
                dcat:downloadURL <https://testdirektoratet.no/download> .

            <https://www.iana.org/assignments/media-types/text/csv>
                a               dct:MediaType ;
                dct:identifier  "text/csv" ;
                dct:title       "csv" .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset ;
                dcat:distribution   <https://testdirektoratet.no/model/distribution> .
        """.trimIndent()

        val expected = """[
            {
              "uri": "https://testdirektoratet.no/model/distribution",
              "title": {
                "no": null,
                "nb": null,
                "nn": null,
                "en": "Distribution"
              },
              "description": null,
              "accessURL": [
                "https://testdirektoratet.no/access"
              ],
              "downloadURL": [
                "https://testdirektoratet.no/download"
              ],
              "license": null,
              "conformsTo": null,
              "page": null,
              "fdkFormat": [
                {
                  "uri": "https://www.iana.org/assignments/media-types/text/csv",
                  "name": "csv",
                  "code": "text/csv",
                  "type": "MEDIA_TYPE"
                }
              ],
              "compressFormat": null,
              "packageFormat": null,
              "accessService": null
            }
        ]""".trimIndent()

        val result = handler.parseDataset("a1c680ca-62d7-34d5-aa4c-d39b5db033ae", turtle)
        result.get("distribution").toString().shouldEqualJson(expected)
    }

    @Test
    fun parseIsAbleToHandleDatasetsNotSupportedByAllVersions() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix schema:  <http://schema.org/> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a           dcat:DatasetSeries ;
                dct:title   "title no"@no , "title nb"@nb , "title nn"@nn , "title en"@en .
        """.trimIndent()

        val expected = """{
            "no": "title no",
            "nb": "title nb",
            "nn": "title nn",
            "en": "title en"
        }""".trimIndent()

        val result = handler.parseDataset("a1c680ca-62d7-34d5-aa4c-d39b5db033ae", turtle)
        result.get("title").toString().shouldEqualJson(expected)
    }

}
