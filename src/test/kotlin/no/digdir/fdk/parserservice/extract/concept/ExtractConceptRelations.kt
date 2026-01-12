package no.digdir.fdk.parserservice.extract.concept

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.concept.ConceptAssociativeRelation
import no.digdir.fdk.model.concept.ConceptGenericRelation
import no.digdir.fdk.model.concept.ConceptPartitiveRelation
import no.digdir.fdk.parserservice.parser.concept.SkosApNoV2Parser
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractConceptRelations {
    val conceptIRI = "https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1"
    val fdkId = "fdk-1"
    val parser = SkosApNoV2Parser()

    @Test
    fun extractsRelationshipsCorrectly() {
        val turtle =
            """
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
            @prefix uneskos: <http://purl.org/umu/uneskos#> .

            <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                a                   skos:Concept ;
                dct:identifier      "concept-1" ;
                skos:prefLabel      "Test concept"@nb ;
                skos:exactMatch     <https://example.org/exact> ;
                skos:closeMatch     <https://example.org/close> ;
                rdfs:seeAlso        <https://example.org/seealso> ;
                dct:isReplacedBy    <https://example.org/replacedby> ;
                dct:replaces        <https://example.org/replaces> ;
                uneskos:memberOf    <https://example.org/memberof> .

            <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                a                  dcat:CatalogRecord ;
                dct:identifier     "fdk-1" ;
                foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, conceptIRI, fdkId)

        assertEquals(listOf("https://example.org/exact"), result.exactMatch)
        assertEquals(listOf("https://example.org/close"), result.closeMatch)
        assertEquals(listOf("https://example.org/seealso"), result.seeAlso)
        assertEquals(listOf("https://example.org/replacedby"), result.isReplacedBy)
        assertEquals(listOf("https://example.org/replaces"), result.replaces)
        assertEquals(listOf("https://example.org/memberof"), result.memberOf)
    }

    @Test
    fun extractsAssociativeRelationsCorrectly() {
        val turtle =
            """
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .

            <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                a                           skos:Concept ;
                dct:identifier              "concept-1" ;
                skos:prefLabel              "Test concept"@nb ;
                skosno:isFromConceptIn      [ a                      skosno:AssociativeRelation ;
                                              skosno:relationRole    "related to"@en ;
                                              skosno:hasToConcept    "https://example.org/concept-2" ] .

            <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                a                  dcat:CatalogRecord ;
                dct:identifier     "fdk-1" ;
                foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, conceptIRI, fdkId)

        val expected =
            ConceptAssociativeRelation().apply {
                description = LocalizedStrings().apply { en = "related to" }
                related = "https://example.org/concept-2"
            }

        assertEquals(listOf(expected), result.associativeRelation)
    }

    @Test
    fun extractsPartitiveRelationsCorrectly() {
        val turtle =
            """
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .

            <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                a                                   skos:Concept ;
                dct:identifier                      "concept-1" ;
                skos:prefLabel                      "Test concept"@nb ;
                skosno:hasPartitiveConceptRelation  [ a                                 skosno:PartitiveRelation ;
                                                      dct:description                   "Part of relation"@en ;
                                                      skosno:hasPartitiveConcept        "https://example.org/concept-part" ;
                                                      skosno:hasComprehensiveConcept    "https://example.org/concept-whole" ] .

            <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                a                  dcat:CatalogRecord ;
                dct:identifier     "fdk-1" ;
                foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, conceptIRI, fdkId)

        val expected =
            ConceptPartitiveRelation().apply {
                description = LocalizedStrings().apply { en = "Part of relation" }
                hasPart = "https://example.org/concept-part"
                isPartOf = "https://example.org/concept-whole"
            }

        assertEquals(listOf(expected), result.partitiveRelation)
    }

    @Test
    fun extractsGenericRelationsCorrectly() {
        val turtle =
            """
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix skosno: <https://data.norge.no/vocabulary/skosno#> .

            <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1>
                a                                   skos:Concept ;
                dct:identifier                      "concept-1" ;
                skos:prefLabel                      "Test concept"@nb ;
                skosno:hasGenericConceptRelation    [ a                             skosno:GenericRelation ;
                                                      dct:description               "Generic relation"@en ;
                                                      skosno:hasSpecificConcept     "https://example.org/concept-specific" ;
                                                      skosno:hasGenericConcept      "https://example.org/concept-generic" ] .

            <https://concepts.staging.fellesdatakatalog.digdir.no/concepts/fdk-1>
                a                  dcat:CatalogRecord ;
                dct:identifier     "fdk-1" ;
                foaf:primaryTopic  <https://registrering-begrep-api.staging.fellesdatakatalog.digdir.no/910258028/concept-1> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m, conceptIRI, fdkId)

        val expected =
            ConceptGenericRelation().apply {
                generalizes = "https://example.org/concept-specific"
                specializes = "https://example.org/concept-generic"
                divisioncriterion = LocalizedStrings().apply { en = "Generic relation" }
            }

        assertEquals(listOf(expected), result.genericRelation)
    }
}
