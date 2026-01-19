package no.digdir.fdk.parserservice.handler

import io.kotest.assertions.json.shouldEqualJson
import no.digdir.fdk.parserservice.parser.InformationModelParserRegistry
import no.digdir.fdk.parserservice.parser.informationmodel.ModellDcatApNoV1Parser
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Tag("unit")
class InformationModelHandlerTest {
    private val parserRegistry = InformationModelParserRegistry()
    private val handler = InformationModelHandler(parserRegistry)

    init {
        parserRegistry.registerParser(ModellDcatApNoV1Parser(), priority = 100, name = "ModellDCAT-AP-NO-V1")
    }

    @Test
    fun `should parse and encode information model`() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .
            @prefix owl:   <http://www.w3.org/2002/07/owl#> .
            @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .

            <https://testdirektoratet.no/model/catalog>
                a               dcat:Catalog ;
                dct:identifier  <https://id.no> ;
                modelldcatno:model <http://test.fellesdatakatalog.digdir.no/information-model/test> ;
                dct:title       "Katalog"@no ;
                dct:description "Beskrivelse av katalog"@no ;
                dct:publisher   <https://testdirektoratet.no/publisher> .

            <https://testdirektoratet.no/publisher>
                a               foaf:Agent ;
                dct:identifier  "112233445" ;
                foaf:name       "Digitaliseringsdirektoratet" .

            <http://test.fellesdatakatalog.digdir.no/information-model/test>
                a                         modelldcatno:InformationModel ;
                dct:title                 "Test Informasjonsmodell"@no ;
                dct:title                 "Test Information Model"@en ;
                dct:description           "Beskrivelse av informasjonsmodell"@no ;
                dct:description           "Description of information model"@en ;
                dct:publisher             <https://testdirektoratet.no/publisher> ;
                dct:identifier            "test-identifier" ;
                dct:issued                "2023-01-01"^^xsd:date ;
                dct:modified              "2023-01-02"^^xsd:date ;
                foaf:homepage             <https://example.com/info-model> ;
                dct:accessRights          <http://publications.europa.eu/resource/authority/access-right/PUBLIC> ;
                dct:language              <http://publications.europa.eu/resource/authority/language/NOB> ;
                dcat:keyword              "modell"@no ;
                dcat:keyword              "model"@en ;
                dcat:theme                <https://psi.norge.no/los/tema/skole-og-utdanning> ;
                dcat:contactPoint         <http://test.fellesdatakatalog.digdir.no/information-model/test#contact> ;
                modelldcatno:informationModelIdentifier "https://www.digdir.no/test-model" ;
                dct:type                  "Fellesmodell"@no ;
                owl:versionInfo           "1.0" ;
                dct:conformsTo            <https://statswiki.unece.org/display/gsim/Generic+Statistical+Information+Model> .

            <http://test.fellesdatakatalog.digdir.no/information-model/test#contact>
                a                         vcard:Organization ;
                vcard:fn                  "Test Contact" ;
                vcard:hasEmail            <mailto:test@example.com> ;
                vcard:hasTelephone        <tel:+4712345678> .

            <https://psi.norge.no/los/tema/skole-og-utdanning>
                a                         skos:Concept ;
                skos:prefLabel            "Skole og utdanning"@no .

            <https://testdirektoratet.no/catalogs/321>
                a                  dcat:CatalogRecord ;
                dct:identifier     "d6199127-8835-33e1-9108-233cd81e92f9" ;
                dct:issued         "2020-06-22T13:39:27.334Z"^^xsd:dateTime ;
                dct:modified       "2020-06-22T13:39:27.334Z"^^xsd:dateTime ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/catalog> .

            <https://testdirektoratet.no/informationmodels/000>
                a                  dcat:CatalogRecord ;
                dct:identifier     "d1d698ef-267a-3d57-949f-b2bc44657f3e" ;
                dct:isPartOf       <https://testdirektoratet.no/catalogs/321> ;
                dct:issued         "2020-06-22T13:39:27.353Z"^^xsd:dateTime ;
                dct:modified       "2020-06-22T13:39:27.353Z"^^xsd:dateTime ;
                foaf:primaryTopic  <http://test.fellesdatakatalog.digdir.no/information-model/test> .
            """.trimIndent()

        val expected =
            """
            {
              "id": "d1d698ef-267a-3d57-949f-b2bc44657f3e",
              "uri": "http://test.fellesdatakatalog.digdir.no/information-model/test",
              "identifier": [
                "test-identifier"
              ],
              "harvest": {
                "firstHarvested": "2020-06-22T13:39:27.353Z",
                "modified": "2020-06-22T13:39:27.353Z"
              },
              "catalog": {
                "id": "https://id.no",
                "uri": "https://testdirektoratet.no/model/catalog",
                "publisher": {
                  "id": "112233445",
                  "uri": "https://testdirektoratet.no/publisher",
                  "name": "Digitaliseringsdirektoratet",
                  "orgPath": null,
                  "title": {
                    "no": "Digitaliseringsdirektoratet",
                    "nb": null,
                    "nn": null,
                    "en": null
                  },
                  "prefLabel": {
                    "no": "Digitaliseringsdirektoratet",
                    "nb": null,
                    "nn": null,
                    "en": null
                  },
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
                "no": "Test Informasjonsmodell",
                "nb": null,
                "nn": null,
                "en": "Test Information Model"
              },
              "description": {
                "no": "Beskrivelse av informasjonsmodell",
                "nb": null,
                "nn": null,
                "en": "Description of information model"
              },
              "descriptionFormatted": {
                "no": "Beskrivelse av informasjonsmodell",
                "nb": null,
                "nn": null,
                "en": "Description of information model"
              },
              "publisher": {
                "id": "112233445",
                "uri": "https://testdirektoratet.no/publisher",
                "name": "Digitaliseringsdirektoratet",
                "orgPath": null,
                "prefLabel": {
                  "no": "Digitaliseringsdirektoratet",
                  "nb": null,
                  "nn": null,
                  "en": null
                },
                "title": {
                  "no": "Digitaliseringsdirektoratet",
                  "nb": null,
                  "nn": null,
                  "en": null
                },
                "organisasjonsform": null
              },
              "dctType": "Fellesmodell",
              "conformsTo": [
                {
                  "uri": "https://statswiki.unece.org/display/gsim/Generic+Statistical+Information+Model",
                  "title": null,
                  "seeAlso": null,
                  "versionInfo": null
                }
              ],
              "license": null,
              "informationModelIdentifier": "https://www.digdir.no/test-model",
              "spatial": null,
              "isPartOf": null,
              "hasPart": null,
              "isReplacedBy": null,
              "isProfileOf": null,
              "replaces": null,
              "temporal": null,
              "hasFormat": null,
              "homepage": "https://example.com/info-model",
              "status": null,
              "versionInfo": "1.0",
              "versionNotes": null,
              "subjects": null,
              "containsSubjects": null,
              "containsModelElements": null,
              "modelElements": null,
              "modelProperties": null,
              "themeUris": [
                "https://psi.norge.no/los/tema/skole-og-utdanning"
              ],
              "theme": null,
              "losTheme": [
                {
                  "uri": "https://psi.norge.no/los/tema/skole-og-utdanning",
                  "code": "skole-og-utdanning",
                  "isTema": true,
                  "losPaths": null,
                  "name": {
                    "no": "Skole og utdanning",
                    "nb": null,
                    "nn": null,
                    "en": null
                  }
                }
              ],
              "eurovocThemes": null,
              "keyword": [
                {
                  "no": "modell",
                  "nb": null,
                  "nn": null,
                  "en": null
                },
                {
                  "no": null,
                  "nb": null,
                  "nn": null,
                  "en": "model"
                }
              ],
              "contactPoint": [
                {
                  "uri": "http://test.fellesdatakatalog.digdir.no/information-model/test#contact",
                  "formattedName": {
                    "no": "Test Contact",
                    "nb": null,
                    "nn": null,
                    "en": null
                  },
                  "fullname": "Test Contact",
                  "email": "test@example.com",
                  "hasURL": null,
                  "hasTelephone": "+4712345678",
                  "organizationName": null,
                  "organizationUnit": null
                }
              ],
              "issued": "2023-01-01",
              "modified": "2023-01-02",
              "language": [
                {
                  "uri": "http://publications.europa.eu/resource/authority/language/NOB",
                  "code": null,
                  "prefLabel": null
                }
              ],
              "type": "informationmodels"
            }
            """.trimIndent()

        val result = handler.parseInformationModel("d1d698ef-267a-3d57-949f-b2bc44657f3e", turtle)

        result.toString().shouldEqualJson(expected)
    }

    @Test
    fun `should throw exception when no information model found`() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .

            <https://testdirektoratet.no/model/catalog>
                a               dcat:Catalog ;
                dct:title       "Katalog"@no .
            """.trimIndent()

        assertThrows<no.digdir.fdk.parserservice.model.NoAcceptableFDKRecordsException> {
            handler.parseInformationModel("non-existent-id", turtle)
        }
    }
}
