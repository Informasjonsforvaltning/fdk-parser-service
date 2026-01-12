package no.digdir.fdk.parserservice.handler

import io.kotest.assertions.json.shouldEqualJson
import no.digdir.fdk.parserservice.parser.ConceptParserRegistry
import no.digdir.fdk.parserservice.parser.concept.SkosApNoV2Parser
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@Tag("unit")
class ConceptHandlerTest {
    private val parserRegistry = ConceptParserRegistry()
    private val handler = ConceptHandler(parserRegistry)

    init {
        parserRegistry.registerParser(SkosApNoV2Parser(), priority = 100, name = "SKOS-AP-NO-V2")
    }

    @Test
    fun parseSimpleConcept() {
        val turtle =
            """
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                a                   skos:Concept ;
                dct:identifier      "concept-1" ;
                skos:prefLabel      "Test concept"@nb ;
                skos:definition     "A concept used for testing"@nb ;
                dct:publisher       <https://data.brreg.no/enhetsregisteret/api/enheter/910258028> .

            <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                a                  dcat:CatalogRecord ;
                dct:identifier     "fdk-1" ;
                dct:issued         "2021-02-17T09:39:13.293Z"^^xsd:dateTime ;
                dct:modified       "2021-02-17T09:39:13.293Z"^^xsd:dateTime ;
                foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
            """.trimIndent()

        val result = handler.parseConcept("fdk-1", turtle)

        val expected = """{
          "id": "fdk-1",
          "uri": "https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1",
          "identifier": "concept-1",
          "harvest": {
            "firstHarvested": "2021-02-17T09:39:13.293Z",
            "modified": "2021-02-17T09:39:13.293Z"
          },
          "collection": null,
          "publisher": {
            "id": null,
            "uri": "https://data.brreg.no/enhetsregisteret/api/enheter/910258028",
            "name": null,
            "orgPath": null,
            "title": null,
            "prefLabel": null,
            "organisasjonsform": null
          },
          "creator": null,
          "subject": null,
          "status": null,
          "example": null,
          "prefLabel": {
            "no": null,
            "nb": "Test concept",
            "nn": null,
            "en": null
          },
          "hiddenLabel": null,
          "altLabel": null,
          "contactPoint": null,
          "definition": {
            "text": {
              "no": null,
              "nb": "A concept used for testing",
              "nn": null,
              "en": null
            },
            "targetGroup": null,
            "sourceRelationship": null,
            "sources": null
          },
          "definitions": [
            {
              "text": {
                "no": null,
                "nb": "A concept used for testing",
                "nn": null,
                "en": null
              },
              "targetGroup": null,
              "sourceRelationship": null,
              "sources": null
            }
          ],
          "seeAlso": null,
          "isReplacedBy": null,
          "replaces": null,
          "validFromIncluding": null,
          "validToIncluding": null,
          "associativeRelation": null,
          "partitiveRelation": null,
          "genericRelation": null,
          "created": null,
          "exactMatch": null,
          "closeMatch": null,
          "memberOf": null,
          "remark": null,
          "range": null,
          "type": "concept"
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

            <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" .
            """.trimIndent()

        assertThrows<Exception> {
            handler.parseConcept("fdk-1", turtle)
        }
    }

    @Test
    fun parseConceptWithAllProperties() {
        val turtle =
            """
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
            @prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .
            @prefix dc:    <http://purl.org/dc/elements/1.1/> .
            @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .

            <https://example.org/collection>
                a                   skos:Collection ;
                dct:identifier      "collection-1" ;
                dct:title           "Test Collection"@en ;
                skos:member         <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .

            <https://example.org/subject>
                a               skos:Concept ;
                skos:prefLabel  "Subject"@en .

            <https://example.org/status/published>
                a               skos:Concept ;
                dc:identifier   "published" ;
                skos:prefLabel  "Published"@en .

            <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                a                   skos:Concept ;
                dct:identifier      "concept-1" ;
                skos:prefLabel      "Test concept"@nb ;
                skos:altLabel       "Alternative label"@nb ;
                skos:hiddenLabel    "Hidden label"@nb ;
                skos:definition     "A concept definition"@nb ;
                skos:example        "Example usage"@en ;
                skos:scopeNote      "Remark about concept"@en ;
                dct:subject         <https://example.org/subject> ;
                dct:publisher       <https://data.brreg.no/enhetsregisteret/api/enheter/910258028> ;
                dct:creator         <https://data.brreg.no/enhetsregisteret/api/enheter/987654321> ;
                euvoc:status        <https://example.org/status/published> ;
                dct:created         "2021-01-01T00:00:00Z"^^xsd:dateTime ;
                euvoc:startDate     "2021-02-01" ;
                euvoc:endDate       "2023-12-31" ;
                skos:exactMatch     <https://example.org/exact> ;
                skos:closeMatch     <https://example.org/close> ;
                rdfs:seeAlso        <https://example.org/seealso> ;
                dct:isReplacedBy    <https://example.org/replacedby> ;
                dct:replaces        <https://example.org/replaces> ;
                dcat:contactPoint   [ a                     vcard:Organization ;
                                      vcard:hasEmail        <mailto:test@example.org> ] .

            <https://data.brreg.no/enhetsregisteret/api/enheter/910258028>
                a               foaf:Agent ;
                dct:identifier  "910258028" .

            <https://data.brreg.no/enhetsregisteret/api/enheter/987654321>
                a               foaf:Agent ;
                dct:identifier  "987654321" .

            <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-2>
                a                  dcat:CatalogRecord ;
                dct:identifier     "fdk-2" ;
                dct:issued         "2021-02-17T09:39:13.293Z"^^xsd:dateTime ;
                dct:modified       "2021-03-17T10:40:14.394Z"^^xsd:dateTime ;
                foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
            """.trimIndent()

        val result = handler.parseConcept("fdk-2", turtle)

        val expected = """{
          "id": "fdk-2",
          "uri": "https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1",
          "identifier": "concept-1",
          "harvest": {
            "firstHarvested": "2021-02-17T09:39:13.293Z",
            "modified": "2021-03-17T10:40:14.394Z"
          },
          "collection": {
            "uri": "https://example.org/collection",
            "id": "collection-1",
            "label": {
              "no": null,
              "nb": null,
              "nn": null,
              "en": "Test Collection"
            },
            "description": null,
            "publisher": null
          },
          "publisher": {
            "id": "910258028",
            "uri": "https://data.brreg.no/enhetsregisteret/api/enheter/910258028",
            "name": null,
            "orgPath": null,
            "title": null,
            "prefLabel": null,
            "organisasjonsform": null
          },
          "creator": {
            "id": "987654321",
            "uri": "https://data.brreg.no/enhetsregisteret/api/enheter/987654321",
            "name": null,
            "orgPath": null,
            "title": null,
            "prefLabel": null,
            "organisasjonsform": null
          },
          "subject": [
            {
              "uri": "https://example.org/subject",
              "label": {
                "no": null,
                "nb": null,
                "nn": null,
                "en": "Subject"
              }
            }
          ],
          "status": {
            "no": null,
            "nb": null,
            "nn": null,
            "en": "Published"
          },
          "example": {
            "no": null,
            "nb": null,
            "nn": null,
            "en": "Example usage"
          },
          "prefLabel": {
            "no": null,
            "nb": "Test concept",
            "nn": null,
            "en": null
          },
          "hiddenLabel": [
            {
              "no": null,
              "nb": "Hidden label",
              "nn": null,
              "en": null
            }
          ],
          "altLabel": [
            {
              "no": null,
              "nb": "Alternative label",
              "nn": null,
              "en": null
            }
          ],
          "contactPoint": {
            "uri": null,
            "email": "test@example.org",
            "formattedName": null,
            "fullname": null,
            "hasURL": null,
            "hasTelephone": null,
            "organizationName": null,
            "organizationUnit": null
          },
          "definition": {
            "text": {
              "no": null,
              "nb": "A concept definition",
              "nn": null,
              "en": null
            },
            "targetGroup": null,
            "sourceRelationship": null,
            "sources": null
          },
          "definitions": [
            {
              "text": {
                "no": null,
                "nb": "A concept definition",
                "nn": null,
                "en": null
              },
              "targetGroup": null,
              "sourceRelationship": null,
              "sources": null
            }
          ],
          "seeAlso": [
            "https://example.org/seealso"
          ],
          "isReplacedBy": [
            "https://example.org/replacedby"
          ],
          "replaces": [
            "https://example.org/replaces"
          ],
          "validFromIncluding": "2021-02-01",
          "validToIncluding": "2023-12-31",
          "associativeRelation": null,
          "partitiveRelation": null,
          "genericRelation": null,
          "created": "2021-01-01T00:00:00Z",
          "exactMatch": [
            "https://example.org/exact"
          ],
          "closeMatch": [
            "https://example.org/close"
          ],
          "memberOf": null,
          "remark": {
            "no": null,
            "nb": null,
            "nn": null,
            "en": "Remark about concept"
          },
          "range": null,
          "type": "concept"
        }"""

        result.toString().shouldEqualJson(expected)
    }

    @Test
    fun parseConceptWithRelations() {
        val turtle =
            """
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .

            <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                a                                   skos:Concept ;
                dct:identifier                      "concept-1" ;
                skos:prefLabel                      "Test concept"@nb ;
                skosno:isFromConceptIn              [ a                      skosno:AssociativeRelation ;
                                                      skosno:relationRole    "related to"@en ;
                                                      skosno:hasToConcept    "https://example.org/concept-2" ] ;
                skosno:hasPartitiveConceptRelation  [ a                                 skosno:PartitiveRelation ;
                                                      dct:description                   "Part of relation"@en ;
                                                      skosno:hasPartitiveConcept        "https://example.org/concept-part" ;
                                                      skosno:hasComprehensiveConcept    "https://example.org/concept-whole" ] ;
                skosno:hasGenericConceptRelation    [ a                             skosno:GenericRelation ;
                                                      dct:description               "Generic relation"@en ;
                                                      skosno:hasSpecificConcept     "https://example.org/concept-specific" ;
                                                      skosno:hasGenericConcept      "https://example.org/concept-generic" ] .

            <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-3>
                a                  dcat:CatalogRecord ;
                dct:identifier     "fdk-3" ;
                foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
            """.trimIndent()

        val result = handler.parseConcept("fdk-3", turtle)

        val expectedAssociative = """[
            {
              "description": {
                "no": null,
                "nb": null,
                "nn": null,
                "en": "related to"
              },
              "related": "https://example.org/concept-2"
            }
          ]"""

        val expectedPartitive = """[
            {
              "description": {
                "no": null,
                "nb": null,
                "nn": null,
                "en": "Part of relation"
              },
              "hasPart": "https://example.org/concept-part",
              "isPartOf": "https://example.org/concept-whole"
            }
          ]"""

        val expectedGeneric = """[
            {
              "divisioncriterion": {
                "no": null,
                "nb": null,
                "nn": null,
                "en": "Generic relation"
              },
              "generalizes": "https://example.org/concept-specific",
              "specializes": "https://example.org/concept-generic"
            }
          ]"""

        result.get("associativeRelation").toString().shouldEqualJson(expectedAssociative)
        result.get("partitiveRelation").toString().shouldEqualJson(expectedPartitive)
        result.get("genericRelation").toString().shouldEqualJson(expectedGeneric)
    }
}
