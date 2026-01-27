package no.digdir.fdk.parserservice.extract.concept

import no.digdir.fdk.model.ContactPoint
import no.digdir.fdk.model.HarvestMetaData
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.Organization
import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.UriWithText
import no.digdir.fdk.model.concept.ConceptCollection
import no.digdir.fdk.model.concept.ConceptSubject
import no.digdir.fdk.parserservice.parser.concept.SkosApNoV1Parser
import no.digdir.fdk.parserservice.parser.concept.SkosApNoV2Parser
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class ConceptExtractionTest {
    val conceptIRI = "https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1"
    val fdkId = "fdk-1"

    @Nested
    inner class V2Tests {
        val parser = SkosApNoV2Parser()

        @Test
        fun extractsBasicPropertiesCorrectly() {
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
                    skos:prefLabel      "Test concept"@nb .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2021-02-17T09:39:13.293Z"^^xsd:dateTime ;
                    dct:modified       "2021-02-17T09:39:13.293Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            assertEquals("fdk-1", result.id)
            assertEquals("https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1", result.uri)
            assertEquals("concept-1", result.identifier)
            assertEquals(ResourceType.concept, result.type)

            val expectedHarvest =
                HarvestMetaData().apply {
                    firstHarvested = "2021-02-17T09:39:13.293Z"
                    modified = "2021-02-17T09:39:13.293Z"
                }

            assertEquals(expectedHarvest, result.harvest)
        }

        @Test
        fun handlesConceptWithoutOptionalFields() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    skos:prefLabel      "Minimal concept"@nb .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            assertEquals("fdk-1", result.id)
            assertEquals("https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1", result.uri)
            assertNotNull(result.prefLabel)
            assertEquals("Minimal concept", result.prefLabel.nb)

            assertNull(result.creator)
            assertNull(result.subject)
            assertNull(result.altLabel)
            assertNull(result.hiddenLabel)
            assertNull(result.definitions)
            assertNull(result.example)
            assertNull(result.remark)
            assertNull(result.collection)
        }

        @Test
        fun extractsLabelsCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Preferred label no"@no ,
                                        "Preferred label nb"@nb ,
                                        "Preferred label nn"@nn ,
                                        "Preferred label en"@en ;
                    skos:altLabel       "Alternative label 1"@nb , "Alternative label 2"@nb ;
                    skos:hiddenLabel    "Hidden label 1"@en , "Hidden label 2"@en .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expectedPrefLabel =
                LocalizedStrings().apply {
                    no = "Preferred label no"
                    nb = "Preferred label nb"
                    nn = "Preferred label nn"
                    en = "Preferred label en"
                }

            assertEquals(expectedPrefLabel, result.prefLabel)

            val expectedAltLabels =
                listOf(
                    LocalizedStrings().apply { nb = "Alternative label 1" },
                    LocalizedStrings().apply { nb = "Alternative label 2" },
                )

            assertEquals(expectedAltLabels.size, result.altLabel.size)
            assertTrue { result.altLabel.containsAll(expectedAltLabels) }

            val expectedHiddenLabels =
                listOf(
                    LocalizedStrings().apply { en = "Hidden label 1" },
                    LocalizedStrings().apply { en = "Hidden label 2" },
                )

            assertEquals(expectedHiddenLabels.size, result.hiddenLabel.size)
            assertTrue { result.hiddenLabel.containsAll(expectedHiddenLabels) }
        }

        @Test
        fun extractsPublisherAndCreatorCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Test concept"@nb ;
                    dct:publisher       <https://data.brreg.no/enhetsregisteret/api/enheter/910258028> ;
                    dct:creator         <https://data.brreg.no/enhetsregisteret/api/enheter/123456789> .

                <https://data.brreg.no/enhetsregisteret/api/enheter/910258028>
                    a               foaf:Agent ;
                    dct:identifier  "910258028" .

                <https://data.brreg.no/enhetsregisteret/api/enheter/123456789>
                    a               foaf:Agent ;
                    dct:identifier  "123456789" .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expectedPublisher =
                Organization().apply {
                    uri = "https://data.brreg.no/enhetsregisteret/api/enheter/910258028"
                    id = "910258028"
                }

            assertEquals(expectedPublisher, result.publisher)

            val expectedCreator =
                Organization().apply {
                    uri = "https://data.brreg.no/enhetsregisteret/api/enheter/123456789"
                    id = "123456789"
                }

            assertEquals(expectedCreator, result.creator)
        }

        @Test
        fun extractsCollectionCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

                <https://example.org/collection>
                    a                   skos:Collection ;
                    dct:identifier      "collection-1" ;
                    dct:title           "Test Collection"@en , "Testsamling"@nb ;
                    dct:description     "A collection of concepts"@en ;
                    dct:publisher       <https://data.brreg.no/enhetsregisteret/api/enheter/910258028> ;
                    skos:member         <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .

                <https://data.brreg.no/enhetsregisteret/api/enheter/910258028>
                    a               foaf:Agent ;
                    dct:identifier  "910258028" .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Test concept"@nb .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                ConceptCollection().apply {
                    uri = "https://example.org/collection"
                    id = "collection-1"
                    label =
                        LocalizedStrings().apply {
                            en = "Test Collection"
                            nb = "Testsamling"
                        }
                    description =
                        LocalizedStrings().apply {
                            en = "A collection of concepts"
                        }
                    publisher =
                        Organization().apply {
                            uri = "https://data.brreg.no/enhetsregisteret/api/enheter/910258028"
                            id = "910258028"
                        }
                }

            assertEquals(expected, result.collection)
        }

        @Test
        fun extractsSubjectsCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

                <https://example.org/subject-1>
                    a               skos:Concept ;
                    skos:prefLabel  "Subject 1"@en , "Emne 1"@nb .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Test concept"@nb ;
                    dct:subject         <https://example.org/subject-1> .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                ConceptSubject().apply {
                    uri = "https://example.org/subject-1"
                    label =
                        LocalizedStrings().apply {
                            nb = "Emne 1"
                            en = "Subject 1"
                        }
                }

            assertEquals(listOf(expected), result.subject)
        }

        @Test
        fun extractsValueRangeCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Test concept"@nb ;
                    skosno:valueRange   <https://example.org/range-1> ,
                                        "Range value"@en .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                listOf(
                    UriWithText().apply { text = LocalizedStrings().apply { en = "Range value" } },
                    UriWithText().apply { uri = "https://example.org/range-1" },
                )

            assertEquals(expected, result.range)
        }

        @Test
        fun extractsContactPointCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Test concept"@nb ;
                    dcat:contactPoint   [ a                     vcard:Organization ;
                                          vcard:hasEmail        <mailto:test@example.org> ;
                                          vcard:hasTelephone    <tel:+4712345678> ;
                                          vcard:hasURL          <https://example.org> ] .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                ContactPoint().apply {
                    email = "test@example.org"
                    hasTelephone = "+4712345678"
                    hasURL = "https://example.org"
                }

            assertEquals(expected, result.contactPoint)
        }

        @Test
        fun extractsRemarkAndExampleCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Test concept"@nb ;
                    skos:scopeNote      "This is a remark"@en , "Dette er en merknad"@nb ;
                    skos:example        "Example usage"@en , "Eksempel på bruk"@nb .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expectedRemark =
                LocalizedStrings().apply {
                    en = "This is a remark"
                    nb = "Dette er en merknad"
                }
            assertEquals(expectedRemark, result.remark)

            val expectedExample =
                LocalizedStrings().apply {
                    en = "Example usage"
                    nb = "Eksempel på bruk"
                }
            assertEquals(expectedExample, result.example)
        }

        @Test
        fun extractsDateFieldsCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .
                @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                       skos:Concept ;
                    dct:identifier          "concept-1" ;
                    skos:prefLabel          "Test concept"@nb ;
                    dct:created             "2021-01-01T00:00:00Z"^^xsd:dateTime ;
                    euvoc:startDate         "2021-02-01" ;
                    euvoc:endDate           "2023-12-31" .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            assertEquals("2021-01-01T00:00:00Z", result.created)
            assertEquals("2021-02-01", result.validFromIncluding)
            assertEquals("2023-12-31", result.validToIncluding)
        }

        @Test
        fun extractsStatusCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .
                @prefix dc:    <http://purl.org/dc/elements/1.1/> .

                <http://publications.europa.eu/resource/authority/concept-status/DEPRECATED>
                    dc:identifier   "DEPRECATED";
                    skos:prefLabel  "deprecated"@en , "frarådet"@nb .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skos:prefLabel      "Test concept"@nb ;
                    euvoc:status        <http://publications.europa.eu/resource/authority/concept-status/DEPRECATED> .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                LocalizedStrings().apply {
                    en = "deprecated"
                    nb = "frarådet"
                }

            assertEquals(expected, result.status)
        }
    }

    @Nested
    inner class V1Tests {
        val parser = SkosApNoV1Parser()

        @Test
        fun extractsBasicPropertiesCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    dct:issued         "2021-02-17T09:39:13.293Z"^^xsd:dateTime ;
                    dct:modified       "2021-02-17T09:39:13.293Z"^^xsd:dateTime ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            assertEquals("fdk-1", result.id)
            assertEquals("https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1", result.uri)
            assertEquals("concept-1", result.identifier)
            assertEquals(ResourceType.concept, result.type)

            val expectedHarvest =
                HarvestMetaData().apply {
                    firstHarvested = "2021-02-17T09:39:13.293Z"
                    modified = "2021-02-17T09:39:13.293Z"
                }

            assertEquals(expectedHarvest, result.harvest)
        }

        @Test
        fun extractsSkosXlLabelsCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix skosxl: <http://www.w3.org/2008/05/skos-xl#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    skosxl:prefLabel    [ skosxl:literalForm "Preferred label V1"@nb ] ;
                    skosxl:altLabel     [ skosxl:literalForm "Alt label 1"@nb ] ,
                                        [ skosxl:literalForm "Alt label 2"@en ] ;
                    skosxl:hiddenLabel  [ skosxl:literalForm "Hidden label 1"@nb ] .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expectedPrefLabel =
                LocalizedStrings().apply {
                    nb = "Preferred label V1"
                }

            assertEquals(expectedPrefLabel, result.prefLabel)

            assertEquals(2, result.altLabel.size)
            assertTrue { result.altLabel.any { it.nb == "Alt label 1" } }
            assertTrue { result.altLabel.any { it.en == "Alt label 2" } }

            assertEquals(1, result.hiddenLabel.size)
            assertEquals("Hidden label 1", result.hiddenLabel.first().nb)
        }

        @Test
        fun extractsPublisherCorrectlyButNotCreator() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    dct:publisher       <https://data.brreg.no/enhetsregisteret/api/enheter/910258028> ;
                    dct:creator         <https://data.brreg.no/enhetsregisteret/api/enheter/123456789> .

                <https://data.brreg.no/enhetsregisteret/api/enheter/910258028>
                    a               foaf:Agent ;
                    dct:identifier  "910258028" .

                <https://data.brreg.no/enhetsregisteret/api/enheter/123456789>
                    a               foaf:Agent ;
                    dct:identifier  "123456789" .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expectedPublisher =
                Organization().apply {
                    uri = "https://data.brreg.no/enhetsregisteret/api/enheter/910258028"
                    id = "910258028"
                }

            assertEquals(expectedPublisher, result.publisher)
            assertNull(result.creator) // V1 does not support creator
        }

        @Test
        fun extractsSubjectsAsLiteralsOnly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

                <https://example.org/subject-1>
                    a               skos:Concept ;
                    skos:prefLabel  "Subject 1"@en .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    dct:subject         <https://example.org/subject-1> ,
                                        "Literal subject"@nb .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            // V1 only extracts literal subjects, not resource-based subjects
            val expected =
                ConceptSubject().apply {
                    label = LocalizedStrings().apply { nb = "Literal subject" }
                }

            assertEquals(listOf(expected), result.subject)
        }

        @Test
        fun extractsValidityDatesFromTemporal() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix schema: <http://schema.org/> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    dct:temporal        [ schema:startDate  "2020-01-01" ;
                                          schema:endDate    "2020-12-31" ] .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            assertEquals("2020-01-01", result.validFromIncluding)
            assertEquals("2020-12-31", result.validToIncluding)
        }

        @Test
        fun v1DoesNotSupportOptionalV2Fields() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix euvoc: <http://publications.europa.eu/ontology/euvoc#> .
                @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    dct:created         "2021-01-01T00:00:00Z"^^xsd:dateTime ;
                    skos:example        "Example"@en ;
                    skos:scopeNote      "Remark"@en ;
                    skos:exactMatch     <https://example.org/exact> ;
                    skos:closeMatch     <https://example.org/close> ;
                    euvoc:status        <http://example.org/status> .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            // These fields are not supported in V1
            assertNull(result.created)
            assertNull(result.example)
            assertNull(result.remark)
            assertNull(result.exactMatch)
            assertNull(result.closeMatch)
            assertNull(result.status)
            assertNull(result.range)
            assertNull(result.memberOf)
        }

        @Test
        fun extractsCollectionCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .

                <https://example.org/collection>
                    a                   skos:Collection ;
                    dct:identifier      "collection-1" ;
                    rdfs:label          "Test Collection"@en ;
                    dct:description     "A collection"@en ;
                    dct:publisher       <https://data.brreg.no/enhetsregisteret/api/enheter/910258028> ;
                    skos:member         <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .

                <https://data.brreg.no/enhetsregisteret/api/enheter/910258028>
                    a               foaf:Agent ;
                    dct:identifier  "910258028" .

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

            val expected =
                ConceptCollection().apply {
                    uri = "https://example.org/collection"
                    id = "collection-1"
                    label = LocalizedStrings().apply { en = "Test Collection" }
                    description = LocalizedStrings().apply { en = "A collection" }
                    publisher =
                        Organization().apply {
                            uri = "https://data.brreg.no/enhetsregisteret/api/enheter/910258028"
                            id = "910258028"
                        }
                }

            assertEquals(expected, result.collection)
        }

        @Test
        fun extractsContactPointCorrectly() {
            val turtle =
                """
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
                @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .

                <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                    a                   skos:Concept ;
                    dct:identifier      "concept-1" ;
                    dcat:contactPoint   [ a                     vcard:Organization ;
                                          vcard:hasEmail        <mailto:v1@example.org> ;
                                          vcard:hasTelephone    <tel:+4712345678> ] .

                <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                    a                  dcat:CatalogRecord ;
                    dct:identifier     "fdk-1" ;
                    foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val result = parser.parse(m, conceptIRI, fdkId)

            val expected =
                ContactPoint().apply {
                    email = "v1@example.org"
                    hasTelephone = "+4712345678"
                }

            assertEquals(expected, result.contactPoint)
        }
    }
}
