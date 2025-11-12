package no.digdir.fdk.parserservice.handler

import io.kotest.assertions.json.shouldEqualJson
import no.digdir.fdk.parserservice.parser.ServiceParserRegistry
import no.digdir.fdk.parserservice.parser.service.CpsvApNoV0Parser
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Tag("unit")
class ServiceHandlerTest {
    private val parserRegistry = ServiceParserRegistry()
    private val handler = ServiceHandler(parserRegistry)

    init {
        parserRegistry.registerParser(CpsvApNoV0Parser(), priority = 50, name = "CPSV-AP-NO-V0")
    }

    @Test
    fun parseAndEncodeCpsvnoService() {
        val turtle =
            """
            @prefix at:    <http://publications.europa.eu/ontology/authority/> .
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix foaf:   <http://xmlns.com/foaf/0.1/> .
            @prefix org:    <http://www.w3.org/ns/org#> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .
            @prefix vcard:  <http://www.w3.org/2006/vcard/ns#> .
            @prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .

            <https://www.staging.fellesdatakatalog.digdir.no/organizations/exOrganisasjonReduced>
                    a              org:Organization;
                    dct:identifier "https://www.staging.fellesdatakatalog.digdir.no/organizations/exOrganisasjonReduced"^^xsd:anyURI ;
                    dct:title      "Organisasjon i Brønnøysund"@nb ;
                    foaf:homepage  <https://www.bronnoy.organisasjon.no> ;
                    dct:type       <http://purl.org/adms/publishertype/NonGovernmentalOrganisation> ;
                    dct:spatial    <http://publications.europa.eu/resource/authority/country/NOR> ,
                                    <https://data.geonorge.no/administrativeEnheter/kommune/id/172833> .

            <http://purl.org/adms/publishertype/NonGovernmentalOrganisation>
                    a               skos:Concept;
                    skos:notation   "NonGovernmentalOrganisation";
                    skos:prefLabel  "Ikkje-statleg organisasjon"@nn , "Ikke-statlig organisasjon"@nb , "Non-Governmental Organisation"@en .

            <https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteresultatDummy.ttl>
                    rdf:type         cv:Output ;
                    dct:description  "The text is displayed in English."@en , "Teksten blir vist på nynorsk."@nn , "Dette er et dummy tjenesteresultat som kan brukes i forbindelse med testing av CPSV-AP-NO når det er behov for en relasjon til et tjenesteresultat."@nb ;
                    dct:identifier   "https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteresultatDummy.ttl"^^xsd:anyURI ;
                    dct:language     <http://publications.europa.eu/resource/authority/language/ENG> ;
                    dct:title        "Dummy tjenesteresultat"@nb , "Dummy tjenesteresultat"@nn , "Dummy service result"@en .

            <https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exKontaktpunktDummy.ttl>
                    rdf:type cv:ContactPoint;
                    vcard:category  "Kontakt test"@nb ;
                    cv:contactPage <https://example.org/exKontaktside>;
                    cv:email "mailto:postmottak@example.org"^^xsd:anyURI;
                    cv:telephone "tel:+4712345678";
                    vcard:language <http://publications.europa.eu/resource/authority/language/ENG> .

            <https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteDummy.ttl>
                    rdf:type           cpsvno:Service ;
                    cv:ownedBy         <https://www.staging.fellesdatakatalog.digdir.no/organizations/exOrganisasjonReduced> ;
                    dct:description    "The text is displayed in English."@en , "Teksten blir vist på nynorsk."@nn , "Dette er en dummytjeneste som kan brukes i forbindelse med testing av CPSV-AP-NO når det er behov for en relasjon til en tjeneste som det ikke finnes eksempel på ennå."@nb ;
                    dct:identifier     "https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteDummy.ttl"^^xsd:anyURI ;
                    dct:language       <http://publications.europa.eu/resource/authority/language/ENG> ;
                    dct:title          "Dummy service"@en , "Dummytjeneste"@nn , "Dummytjeneste"@nb ;
                    cv:sector          <http://publications.europa.eu/resource/authority/data-theme/GOVE> ;
                    cpsv:produces      <https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteresultatDummy.ttl> ;
                    cv:contactPoint    <https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exKontaktpunktDummy.ttl> ;
                    cv:thematicArea    <https://psi.norge.no/not/in/los/or/eu> ,
                                       <https://psi.norge.no/los/tema/naring> ,
                                       <http://eurovoc.europa.eu/68> .

            <https://www.staging.fellesdatakatalog.digdir.no/public-services/1fc38c3c-1c86-3161-a9a7-e443fd94d413>
                    rdf:type           dcat:CatalogRecord ;
                    dct:identifier     "1fc38c3c-1c86-3161-a9a7-e443fd94d413" ;
                    dct:issued         "2022-05-18T11:26:51.589Z"^^xsd:dateTime ;
                    dct:modified       "2022-05-18T11:26:51.589Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteDummy.ttl> .

            <http://publications.europa.eu/resource/authority/data-theme/GOVE>
                    skos:prefLabel	"Forvaltning og offentleg sektor"@nn ;
                    skos:prefLabel	"Government and public sector"@en ;
                    skos:prefLabel	"Forvaltning og offentlig sektor"@nb ;
                    skos:prefLabel	"Forvaltning og offentlig sektor"@no .

            <https://psi.norge.no/los/tema/naring>
                    rdf:type           skos:Concept ;
                    skos:prefLabel     "Business"@en , "Næring"@nb , "Næring"@nn ;
                    <https://fellesdatakatalog.digdir.no/ontology/internal/themePath>
                            "naring" .

            <http://eurovoc.europa.eu/68>
                rdf:type        skos:Concept;
                skos:broader    <http://eurovoc.europa.eu/77> ;
                skos:prefLabel  "local government"@en;
                <https://fellesdatakatalog.digdir.no/ontology/internal/themePath>
                    "77/68" .

            <http://publications.europa.eu/resource/authority/language/ENG>
                    a                  skos:Concept;
                    at:authority-code      "ENG";
                    skos:prefLabel     "Engelsk"@nb , "Engelsk"@nn , "Engelsk"@no , "English"@en .
            """.trimIndent()

        val result = handler.parseService("1fc38c3c-1c86-3161-a9a7-e443fd94d413", turtle)

        val expected = """{
          "id": "1fc38c3c-1c86-3161-a9a7-e443fd94d413",
          "uri": "https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteDummy.ttl",
          "identifier": "https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteDummy.ttl",
          "title": {
            "no": null,
            "nb": "Dummytjeneste",
            "nn": "Dummytjeneste",
            "en": "Dummy service"
          },
          "description": {
            "no": null,
            "nb": "Dette er en dummytjeneste som kan brukes i forbindelse med testing av CPSV-AP-NO når det er behov for en relasjon til en tjeneste som det ikke finnes eksempel på ennå.",
            "nn": "Teksten blir vist på nynorsk.",
            "en": "The text is displayed in English."
          },
          "harvest": {
            "firstHarvested": "2022-05-18T11:26:51.589Z",
            "modified": "2022-05-18T11:26:51.589Z"
          },
          "catalog": null,
          "ownedBy": [
            {
              "id": "https://www.staging.fellesdatakatalog.digdir.no/organizations/exOrganisasjonReduced",
              "uri": "https://www.staging.fellesdatakatalog.digdir.no/organizations/exOrganisasjonReduced",
              "name": null,
              "orgPath": null,
              "title": {
                "no": null,
                "nb": "Organisasjon i Brønnøysund",
                "nn": null,
                "en": null
              },
              "prefLabel": null,
              "organisasjonsform": null
            }
          ],
          "contactPoint": null,
          "keyword": null,
          "sector": [
            {
              "uri": "http://publications.europa.eu/resource/authority/data-theme/GOVE",
              "code": "GOVE",
              "title": {
                "no": "Forvaltning og offentlig sektor",
                "nb": "Forvaltning og offentlig sektor",
                "nn": "Forvaltning og offentleg sektor",
                "en": "Government and public sector"
              }
            }
          ],
          "produces": [
            {
              "uri": "https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteresultatDummy.ttl",
              "identifier": "https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteresultatDummy.ttl",
              "name": {
                "no": null,
                "nb": "Dummy tjenesteresultat",
                "nn": "Dummy tjenesteresultat",
                "en": "Dummy service result"
              },
              "description": {
                "no": null,
                "nb": "Dette er et dummy tjenesteresultat som kan brukes i forbindelse med testing av CPSV-AP-NO når det er behov for en relasjon til et tjenesteresultat.",
                "nn": "Teksten blir vist på nynorsk.",
                "en": "The text is displayed in English."
              },
              "language": [
                {
                  "uri": "http://publications.europa.eu/resource/authority/language/ENG",
                  "code": "ENG",
                  "prefLabel": {
                    "no": "Engelsk",
                    "nb": "Engelsk",
                    "nn": "Engelsk",
                    "en": "English"
                  }
                }
              ],
              "type": null
            }
          ],
          "spatial": null,
          "hasInput": null,
          "processingTime": null,
          "isDescribedAt": null,
          "hasParticipation": null,
          "isGroupedBy": null,
          "isClassifiedBy": null,
          "hasChannel": null,
          "follows": null,
          "hasCost": null,
          "requires": null,
          "relation": null,
          "hasLegalResource": null,
          "language": [
            {
              "uri": "http://publications.europa.eu/resource/authority/language/ENG",
              "code": "ENG",
              "prefLabel": {
                "no": "Engelsk",
                "nb": "Engelsk",
                "nn": "Engelsk",
                "en": "English"
              }
            }
          ],
          "holdsRequirement": null,
          "admsStatus": null,
          "subject": null,
          "homepage": null,
          "dctType": null,
          "thematicAreaUris": [
            "https://psi.norge.no/los/tema/naring",
            "https://psi.norge.no/not/in/los/or/eu",
            "http://eurovoc.europa.eu/68"
          ],
          "losThemes": [
            {
              "uri": "https://psi.norge.no/los/tema/naring",
              "code": "naring",
              "isTema": true,
              "losPaths": [
                "naring"
              ],
              "name": {
                "no": null,
                "nb": "Næring",
                "nn": "Næring",
                "en": "Business"
              }
            }
          ],
          "eurovocThemes": [
            {
              "uri": "http://eurovoc.europa.eu/68",
              "code": "68",
              "eurovocPaths": [
                "77/68"
              ],
              "label": {
                "no": null,
                "nb": null,
                "nn": null,
                "en": "local government"
              }
            }
          ],
          "participatingAgents": null,
          "hasCompetentAuthority": null,
          "type": "publicservices",
          "specializedType": "service"
        }"""

        result.toString().shouldEqualJson(expected)
    }

    @Test
    fun parseServiceWithEvidenceCollection() {
        val turtle =
            """
            @prefix cpsv:   <http://purl.org/vocab/cpsv#> .
            @prefix cpsvno: <https://data.norge.no/vocabulary/cpsvno#> .
            @prefix cv:     <http://data.europa.eu/m8g/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix foaf:   <http://xmlns.com/foaf/0.1/> .
            @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .

            <https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteDummy.ttl>
                    rdf:type            cpsvno:Service ;
                    dct:identifier      "https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteDummy.ttl"^^xsd:anyURI ;
                    cpsv:hasInput       <http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/1> ,
                                        <http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/3> ,
                                        [
                                            rdf:type cv:Evidence ;
                                            dct:identifier "999999999" ;
                                        ] ;
                    cv:hasChannel       <http://public-service-publisher.fellesdatakatalog.digdir.no/channel/1> .

            <https://www.staging.fellesdatakatalog.digdir.no/public-services/1fc38c3c-1c86-3161-a9a7-e443fd94d413>
                    rdf:type            dcat:CatalogRecord ;
                    dct:identifier      "1fc38c3c-1c86-3161-a9a7-e443fd94d413" ;
                    dct:issued          "2022-05-18T11:26:51.589Z"^^xsd:dateTime ;
                    dct:modified        "2022-05-18T11:26:51.589Z"^^xsd:dateTime ;
                    foaf:primaryTopic   <https://raw.githubusercontent.com/Informasjonsforvaltning/cpsv-ap-no/develop/examples/exTjenesteDummy.ttl> .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/channel/1>
                    a               cv:Channel ;
                    dct:identifier  "1" ;
                    cpsv:hasInput   <http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/2> ,
                                    <http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/3> ,
                                    <http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/4> ,
                                    [
                                        rdf:type cv:Evidence ;
                                        dct:identifier "999999999" ;
                                    ] .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/1>
                    a                cv:Evidence ;
                    dct:description  "Vandelsattest"@nb ;
                    dct:identifier   "1" ;
                    dct:title        "Vandelsattest"@nb .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/2>
                    a                dcat:Dataset ;
                    dct:description  "Annen dokumentasjon"@nb ;
                    dct:identifier   "2" ;
                    dct:title        "Nødvendig dokumentasjon"@nb .

            <http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/3>
                    a                cv:Evidence ;
                    dct:description  "Duplisert dokumentasjon"@nb ;
                    dct:identifier   "3" .
            """.trimIndent()

        val result = handler.parseService("1fc38c3c-1c86-3161-a9a7-e443fd94d413", turtle)

        val expectedEvidence = """[
          {
            "uri": null,
            "identifier": "999999999",
            "rdfType": "http://data.europa.eu/m8g/Evidence",
            "dctType": null,
            "name": null,
            "description": null,
            "language": null,
            "page": null
          },
          {
            "uri": "http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/3",
            "identifier": "3",
            "rdfType": "http://data.europa.eu/m8g/Evidence",
            "dctType": null,
            "name": null,
            "description": {
              "no": null,
              "nb": "Duplisert dokumentasjon",
              "nn": null,
              "en": null
            },
            "language": null,
            "page": null
          },
          {
            "uri": "http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/1",
            "identifier": "1",
            "rdfType": "http://data.europa.eu/m8g/Evidence",
            "dctType": null,
            "name": {
              "no": null,
              "nb": "Vandelsattest",
              "nn": null,
              "en": null
            },
            "description": {
              "no": null,
              "nb": "Vandelsattest",
              "nn": null,
              "en": null
            },
            "language": null,
            "page": null
          },
          {
            "uri": null,
            "identifier": "999999999",
            "rdfType": "http://data.europa.eu/m8g/Evidence",
            "dctType": null,
            "name": null,
            "description": null,
            "language": null,
            "page": null
          },
          {
            "uri": "http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/4",
            "identifier": null,
            "rdfType": null,
            "dctType": null,
            "name": null,
            "description": null,
            "language": null,
            "page": null
          },
          {
            "uri": "http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/2",
            "identifier": "2",
            "rdfType": "http://www.w3.org/ns/dcat#Dataset",
            "dctType": null,
            "name": {
              "no": null,
              "nb": "Nødvendig dokumentasjon",
              "nn": null,
              "en": null
            },
            "description": {
              "no": null,
              "nb": "Annen dokumentasjon",
              "nn": null,
              "en": null
            },
            "language": null,
            "page": null
          }
        ]"""
        result.get("hasInput").toString().shouldEqualJson(expectedEvidence)

        val expectedChannel = """[
          {
            "uri": "http://public-service-publisher.fellesdatakatalog.digdir.no/channel/1",
            "identifier": "1",
            "channelType": null,
            "description": null,
            "processingTime": null,
            "hasInput": [
              "http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/4",
              "http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/3",
              "http://public-service-publisher.fellesdatakatalog.digdir.no/evidence/2"
            ],
            "email": null,
            "url": null,
            "telephone": null
          }
        ]"""
        result.get("hasChannel").toString().shouldEqualJson(expectedChannel)
    }

    @Test
    fun exceptionWhenCatalogRecordHasNoPrimaryTopic() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <https://www.staging.fellesdatakatalog.digdir.no/public-services/1fc38c3c-1c86-3161-a9a7-e443fd94d413>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "1fc38c3c-1c86-3161-a9a7-e443fd94d413" .
            """.trimIndent()

        assertThrows<Exception> {
            handler.parseService("1fc38c3c-1c86-3161-a9a7-e443fd94d413", turtle)
        }
    }
}
