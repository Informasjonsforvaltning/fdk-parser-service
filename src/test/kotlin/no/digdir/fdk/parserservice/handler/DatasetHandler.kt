package no.digdir.fdk.parserservice.handler

import io.kotest.assertions.json.shouldEqualJson
import no.digdir.fdk.parserservice.parser.DatasetParserRegistry
import no.digdir.fdk.parserservice.parser.DatasetParserStrategy
import no.digdir.fdk.parserservice.parser.dataset.DcatApNoV1Parser
import no.digdir.fdk.parserservice.parser.dataset.DcatApNoV2Parser
import no.digdir.fdk.parserservice.parser.dataset.MobilityDcatApV3Parser
import no.digdir.fdk.model.LocalizedStrings
import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Tag("unit")
class DatasetHandlerTest {
    private val parserRegistry = DatasetParserRegistry()
    private val handler = DatasetHandler(parserRegistry)
    
    init {
        // Register parsers for testing
        parserRegistry.registerParser(MobilityDcatApV3Parser(), priority = 200, name = "MOBILITY-DCAT-AP-V3")
        parserRegistry.registerParser(DcatApNoV2Parser(), priority = 100, name = "DCAT-AP-NO-V2")
        parserRegistry.registerParser(DcatApNoV1Parser(), priority = 50, name = "DCAT-AP-NO-V1")
    }

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

    @Test
    fun mobilityValueOverridesV2OnConflict() {
        val localRegistry = DatasetParserRegistry()

        val mobility = object : DatasetParserStrategy {
            override fun parse(model: Model, iri: String) = no.digdir.fdk.model.dataset.Dataset.newBuilder()
                .setId("id")
                .setUri(iri)
                .setIdentifier(null)
                .setAdmsIdentifier(null)
                .setHarvest(null)
                .setCatalog(null)
                .setTitle(LocalizedStrings(null, null, null, "MOB"))
                .setDescription(null)
                .setDescriptionFormatted(null)
                .setPublisher(null)
                .setDistribution(null)
                .setSample(null)
                .setContactPoint(null)
                .setThemeUris(null)
                .setTheme(null)
                .setLosTheme(null)
                .setEurovocThemes(null)
                .setKeyword(null)
                .setIssued(null)
                .setModified(null)
                .setDctType(null)
                .setAccessRights(null)
                .setLanguage(null)
                .setPage(null)
                .setLandingPage(null)
                .setTemporal(null)
                .setSubject(null)
                .setSpatial(null)
                .setProvenance(null)
                .setAccrualPeriodicity(null)
                .setLegalBasisForAccess(null)
                .setLegalBasisForProcessing(null)
                .setLegalBasisForRestriction(null)
                .setConformsTo(null)
                .setReferences(null)
                .setHasAccuracyAnnotation(null)
                .setHasAvailabilityAnnotation(null)
                .setHasCompletenessAnnotation(null)
                .setHasCurrentnessAnnotation(null)
                .setHasRelevanceAnnotation(null)
                .setQualifiedAttributions(null)
                .setIsOpenData(false)
                .setIsAuthoritative(false)
                .setIsRelatedToTransportportal(false)
                .setInSeries(null)
                .setPrev(null)
                .setLast(null)
                .setDatasetsInSeries(null)
                .setType(null)
                .setSpecializedType(null)
                .build()
            override fun parse(model: Model, iri: String, fdkId: String) = parse(model, iri)
        }

        val v2 = object : DatasetParserStrategy {
            override fun parse(model: Model, iri: String) = no.digdir.fdk.model.dataset.Dataset.newBuilder()
                .setId("id")
                .setUri(iri)
                .setIdentifier(null)
                .setAdmsIdentifier(null)
                .setHarvest(null)
                .setCatalog(null)
                .setTitle(LocalizedStrings(null, null, null, "V2"))
                .setDescription(null)
                .setDescriptionFormatted(null)
                .setPublisher(null)
                .setDistribution(null)
                .setSample(null)
                .setContactPoint(null)
                .setThemeUris(null)
                .setTheme(null)
                .setLosTheme(null)
                .setEurovocThemes(null)
                .setKeyword(null)
                .setIssued(null)
                .setModified(null)
                .setDctType(null)
                .setAccessRights(null)
                .setLanguage(null)
                .setPage(null)
                .setLandingPage(null)
                .setTemporal(null)
                .setSubject(null)
                .setSpatial(null)
                .setProvenance(null)
                .setAccrualPeriodicity(null)
                .setLegalBasisForAccess(null)
                .setLegalBasisForProcessing(null)
                .setLegalBasisForRestriction(null)
                .setConformsTo(null)
                .setReferences(null)
                .setHasAccuracyAnnotation(null)
                .setHasAvailabilityAnnotation(null)
                .setHasCompletenessAnnotation(null)
                .setHasCurrentnessAnnotation(null)
                .setHasRelevanceAnnotation(null)
                .setQualifiedAttributions(null)
                .setIsOpenData(false)
                .setIsAuthoritative(false)
                .setIsRelatedToTransportportal(false)
                .setInSeries(null)
                .setPrev(null)
                .setLast(null)
                .setDatasetsInSeries(null)
                .setType(null)
                .setSpecializedType(null)
                .build()
            override fun parse(model: Model, iri: String, fdkId: String) = parse(model, iri)
        }

        localRegistry.registerParser(mobility, 200, "mob")
        localRegistry.registerParser(v2, 100, "v2")

        val localHandler = DatasetHandler(localRegistry)

        val graph = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            <http://test.fellesdatakatalog.digdir.no/datasets/id>
              a dcat:CatalogRecord ;
              dct:identifier "id" ;
              foaf:primaryTopic <http://example.org/ds> .
            <http://example.org/ds> a dcat:Dataset .
        """.trimIndent()

        val json = localHandler.parseDataset("id", graph)

        val expectedTitle = """{
            "en": "MOB",
            "nb": null,
            "nn": null,
            "no": null
        }""".trimIndent()

        json.get("title").toString().shouldEqualJson(expectedTitle)
    }

}
