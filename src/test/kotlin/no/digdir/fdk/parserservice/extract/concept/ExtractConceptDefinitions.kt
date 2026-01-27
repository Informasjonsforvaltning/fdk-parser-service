package no.digdir.fdk.parserservice.extract.concept

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.UriWithText
import no.digdir.fdk.model.concept.ConceptDefinition
import no.digdir.fdk.parserservice.parser.concept.SkosApNoV1Parser
import no.digdir.fdk.parserservice.parser.concept.SkosApNoV2Parser
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class ExtractConceptDefinitions {
    val conceptIRI = "https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1"
    val fdkId = "fdk-1"

    @Nested
    inner class V2Tests {
        val parser = SkosApNoV2Parser()

        @Test
        fun extractsTextDefinitionCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .
                @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Test concept"@nb ;
                    skos:definition     "definition (direct statement)"@en .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                ConceptDefinition().apply {
                    text = LocalizedStrings().apply { en = "definition (direct statement)" }
                }

            assertEquals(expected, result.definition)
        }

        @Test
        fun extractsXlDefinitionCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .
                @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Test concept"@nb ;
                    euvoc:xlDefinition  [ a                            euvoc:XlNote ;
                                          rdf:value                    "definition (via definition object)"@en ;
                                          dct:audience                 <https://data.norge.no/vocabulary/audience-type#public> ;
                                          dct:source                    <https://test-source.no> ;
                                          skosno:relationshipWithSource <https://data.norge.no/vocabulary/relationship-with-source-type#derived-from-source> ] .

                <https://test-source.no>
                    rdfs:label "source label"@en .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                ConceptDefinition().apply {
                    text = LocalizedStrings().apply { en = "definition (via definition object)" }
                    targetGroup = "https://data.norge.no/vocabulary/audience-type#public"
                    sources =
                        listOf(
                            UriWithText().apply {
                                uri = "https://test-source.no"
                                text = LocalizedStrings().apply { en = "source label" }
                            },
                        )
                    sourceRelationship =
                        "https://data.norge.no/vocabulary/relationship-with-source-type#derived-from-source"
                }

            assertEquals(expected, result.definition)
        }

        @Test
        fun definitionWithNoSpecificAudienceIsPrioritized() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .
                @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Test concept"@nb ;
                    skos:definition     "default definition"@en ;
                    euvoc:xlDefinition  [ a                            euvoc:XlNote ;
                                          rdf:value                    "specialist definition"@en ;
                                          dct:audience                 <https://data.norge.no/vocabulary/audience-type#specialist> ] ;
                    euvoc:xlDefinition  [ a                            euvoc:XlNote ;
                                          rdf:value                    "public definition"@en ;
                                          dct:audience                 <https://data.norge.no/vocabulary/audience-type#public> ] .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                ConceptDefinition().apply {
                    text = LocalizedStrings().apply { en = "default definition" }
                }

            assertEquals(expected, result.definition)

            val expectedList =
                listOf(
                    ConceptDefinition().apply {
                        text = LocalizedStrings().apply { en = "default definition" }
                    },
                    ConceptDefinition().apply {
                        text = LocalizedStrings().apply { en = "public definition" }
                        targetGroup = "https://data.norge.no/vocabulary/audience-type#public"
                    },
                    ConceptDefinition().apply {
                        text = LocalizedStrings().apply { en = "specialist definition" }
                        targetGroup = "https://data.norge.no/vocabulary/audience-type#specialist"
                    },
                )

            assertEquals(expectedList.size, result.definitions.size)
            assertTrue { result.definitions.containsAll(expectedList) }
        }
    }

    @Nested
    inner class V1Tests {
        val parser = SkosApNoV1Parser()

        @Test
        fun extractsV1DefinitionCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .
                @prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skosno:definisjon   [ rdfs:label "V1 definition text"@nb ] .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                ConceptDefinition().apply {
                    text = LocalizedStrings().apply { nb = "V1 definition text" }
                }

            assertEquals(expected, result.definition)
        }

        @Test
        fun extractsV1DefinitionWithTargetGroupCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .
                @prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skosno:definisjon   [ rdfs:label         "Public definition"@nb ;
                                          dct:audience       "allmennheten" ] .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                ConceptDefinition().apply {
                    text = LocalizedStrings().apply { nb = "Public definition" }
                    targetGroup = "allmennheten"
                }

            assertEquals(expected, result.definition)
        }

        @Test
        fun extractsV1DefinitionWithSourceCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .
                @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skosno:definisjon   [ rdfs:label     "Definition from source"@nb ;
                                          dct:source     [
                                            rdfs:seeAlso <https://test-source.no> ;
                                            rdfs:label   "V1 source label"@nb
                                          ] ;
                                          skosno:forholdTilKilde skosno:basertPåKilde ] .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                ConceptDefinition().apply {
                    text = LocalizedStrings().apply { nb = "Definition from source" }
                    sources =
                        listOf(
                            UriWithText().apply {
                                uri = "https://test-source.no"
                                text = LocalizedStrings().apply { nb = "V1 source label" }
                            },
                        )
                    sourceRelationship =
                        "basertPåKilde"
                }

            assertEquals(expected, result.definition)
        }

        @Test
        fun extractsMultipleV1DefinitionsCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .
                @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skosno:definisjon   [ rdfs:label "General definition"@nb ] ;
                    skosno:definisjon   [ rdfs:label          "Specialist definition"@nb ;
                                          dct:audience   skosno:fagspesialist ] .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            // Definition without målgruppe is prioritized
            val expectedDef =
                ConceptDefinition().apply {
                    text = LocalizedStrings().apply { nb = "General definition" }
                }

            assertEquals(expectedDef, result.definition)

            val expectedList =
                listOf(
                    ConceptDefinition().apply {
                        text = LocalizedStrings().apply { nb = "General definition" }
                    },
                    ConceptDefinition().apply {
                        text = LocalizedStrings().apply { nb = "Specialist definition" }
                        targetGroup = "fagspesialist"
                    },
                )

            assertEquals(expectedList.size, result.definitions.size)
            assertTrue { result.definitions.containsAll(expectedList) }
        }

        @Test
        fun returnsNullWhenNoV1DefinitionsPresent() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            assertNull(result.definition)
            assertNull(result.definitions)
        }

        @Test
        fun v1DoesNotExtractSkosDefinition() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:definition     "V2 definition"@nb ;
                    euvoc:xlDefinition  [ rdf:value "V2 XL definition"@nb ] .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            // V1 parser only looks for skosno:definisjon, not skos:definition or euvoc:xlDefinition
            assertNull(result.definition)
            assertNull(result.definitions)
        }
    }
}
