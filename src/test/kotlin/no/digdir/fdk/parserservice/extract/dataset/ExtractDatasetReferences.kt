package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.UriWithLabel
import no.digdir.fdk.model.dataset.Reference
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractDatasetReferences {

    @Test
    fun extractListOfReferences() {
        val turtle = """
            @prefix dct:  <http://purl.org/dc/terms/> .
            @prefix dcat: <http://www.w3.org/ns/dcat#> .
            @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                   dcat:Dataset ;
                dct:hasVersion      <https://hasversion.no> ;
                dct:isVersionOf     <https://isversionof.no> ;
                dct:isPartOf        <https://ispartof.no> ;
                dct:hasPart         <https://haspart.no> ;
                dct:references      <https://references.no> ;
                dct:isReferencedBy  <https://isreferencedby.no> ;
                dct:replaces        <https://replaces.no> ;
                dct:isReplacedBy    <https://isreplacedby.no> ;
                dct:requires        <https://requires.no> ;
                dct:isRequiredBy    <https://isrequiredby.no> ;
                dct:source          <https://source.no> ;
                dct:relation        <https://relation.no> .

            <https://relation.no>
                a           rdfs:Resource ;
                rdfs:label  "related"@en .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = listOf(
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/hasVersion" }
                source = UriWithLabel().apply { uri = "https://hasversion.no" }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/isVersionOf" }
                source = UriWithLabel().apply { uri = "https://isversionof.no" }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/isPartOf" }
                source = UriWithLabel().apply { uri = "https://ispartof.no" }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/hasPart" }
                source = UriWithLabel().apply { uri = "https://haspart.no" }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/references" }
                source = UriWithLabel().apply { uri = "https://references.no" }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/isReferencedBy" }
                source = UriWithLabel().apply { uri = "https://isreferencedby.no" }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/replaces" }
                source = UriWithLabel().apply { uri = "https://replaces.no" }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/isReplacedBy" }
                source = UriWithLabel().apply { uri = "https://isreplacedby.no" }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/requires" }
                source = UriWithLabel().apply { uri = "https://requires.no" }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/isRequiredBy" }
                source = UriWithLabel().apply { uri = "https://isrequiredby.no" }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/relation" }
                source = UriWithLabel().apply {
                    uri = "https://relation.no"
                    prefLabel = LocalizedStrings().apply { en = "related" }
                }
            },
            Reference().apply {
                referenceType = ReferenceDataCode().apply { uri = "http://purl.org/dc/terms/source" }
                source = UriWithLabel().apply { uri = "https://source.no" }
            }
        )

        assertEquals(expected, subject.extractListOfReferences())
    }
}
