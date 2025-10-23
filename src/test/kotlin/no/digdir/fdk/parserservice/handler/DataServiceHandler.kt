package no.digdir.fdk.parserservice.handler

import io.kotest.assertions.json.shouldEqualJson
import no.digdir.fdk.parserservice.parser.dataservice.DcatApNoV2Parser
import no.digdir.fdk.parserservice.parser.DataServiceParserRegistry
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class DataServiceHandlerTest {
    private val parserRegistry = DataServiceParserRegistry()
    private val handler = DataServiceHandler(parserRegistry)

    init {
        parserRegistry.registerParser(DcatApNoV2Parser(), priority = 50, name = "DCAT-AP-NO-V2")
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

            <http://test.fellesdatakatalog.digdir.no/data-service/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/data-service/0> .

            <https://testdirektoratet.no/model/data-service/0>
                a                         dcat:DataService ;
                dct:title                 "title"@nb ;
                dct:description           "description"@nb ;
                dct:issued         "2018-01-11T10:50:10.111Z"^^xsd:dateTime ;
                dct:modified       "2020-02-22T12:52:20.222Z"^^xsd:dateTime ;
                dcat:contactPoint [ vcard:hasEmail    "post@mail.com" ] .
        """.trimIndent()

        val expected = """{
            "id": "a1c680ca-62d7-34d5-aa4c-d39b5db033ae",
            "uri": "https://testdirektoratet.no/model/data-service/0",
            "identifier": null,
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
            "accessRights": null,
            "themeUris": null,
            "theme": null,
            "losTheme": null,
            "eurovocThemes": null,
            "keyword": null,
            "contactPoint": [
                {
                  "uri": null,
                  "fullname": null,
                  "email": "post@mail.com",
                  "hasURL": null,
                  "hasTelephone": null,
                  "organizationName": null,
                  "organizationUnit": null
                }
            ],
            "issued": "2018-01-11T10:50:10.111Z",
            "modified": "2020-02-22T12:52:20.222Z",
            "landingPage": null,
            "language": null,
            "harvest": null,
            "catalog": null,
            "dctType": null,
            "endpointDescription": null,
            "endpointURL": null,
            "fdkFormat": null,
            "servesDataset": null,
            "conformsTo": null,
            "page": null,
            "type": "dataservices"
        }""".trimIndent()

        val result = handler.parseDataService("a1c680ca-62d7-34d5-aa4c-d39b5db033ae", turtle)
        result.toString().shouldEqualJson(expected)
    }

}
