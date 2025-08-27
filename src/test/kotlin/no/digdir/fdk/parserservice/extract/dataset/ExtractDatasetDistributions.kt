package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.Format
import no.digdir.fdk.model.FormatType
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.UriWithLabel
import no.digdir.fdk.model.UriWithLabelAndType
import no.digdir.fdk.model.dataset.Distribution
import no.digdir.fdk.parseservice.extract.dataset.extractListOfDistributionsV1
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class ExtractDatasetDistributions {

    @Test
    fun extractDistributionWithAccessUrlDownloadUrlAndUri() {
        val turtle = """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                   dcat:Dataset ;
                dcat:distribution   <https://testdirektoratet.no/model/distribution> .
                
            <https://testdirektoratet.no/model/distribution>
                a                dcat:Distribution ;
                dcat:accessURL   <https://testdirektoratet.no/access> ;
                dcat:downloadURL <https://testdirektoratet.no/download> .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = listOf(
            Distribution().apply {
                uri = "https://testdirektoratet.no/model/distribution"
                accessURL = listOf("https://testdirektoratet.no/access")
                downloadURL = listOf("https://testdirektoratet.no/download")
            }
        )

        assertEquals(expected, subject.extractListOfDistributionsV1(DCAT.distribution))
    }

    @Test
    fun extractDistributionWithTitleAndDescription() {
        val turtle = """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                   dcat:Dataset ;
                dcat:distribution   [  a                dcat:Distribution ;
                                       dct:title        "Distribution"@en ;
                                       dct:description  "Distribution description"@en
                                    ] .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = listOf(
            Distribution().apply {
                title = LocalizedStrings().apply { en = "Distribution" }
                description = LocalizedStrings().apply { en = "Distribution description" }
            }
        )

        assertEquals(expected, subject.extractListOfDistributionsV1(DCAT.distribution))
    }

    @Test
    fun extractDistributionWithConformsToLicenseAndPage() {
        val turtle = """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

            <https://testdirektoratet.no/model/dataset/0>
                a                   dcat:Dataset ;
                dcat:distribution   [  a                dcat:Distribution ;
                                       dct:conformsTo   <https://conforms.to> ;
                                       dct:license      <https://license-one.com> , <https://license-two.com> ;
                                       foaf:page        [
                                            a   foaf:Document ;
                                            skos:prefLabel   "Document label"@en
                                       ]
                                    ] .
            <https://conforms.to>
                dct:title   "Conforms to"@en .

            <https://license-one.com>
                a   dct:LicenseDocument , skos:Concept ;
                skos:prefLabel   "One"@en .

            <https://license-two.com>
                a   skos:Concept ;
                dct:source  <http://source.com> ;
                skos:prefLabel   "Two"@en .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expectedConformsTo = UriWithLabel().apply {
            uri = "https://conforms.to"
            prefLabel = LocalizedStrings().apply { en = "Conforms to" }
        }

        val expectedPage = UriWithLabelAndType().apply {
            extraType = FOAF.Document.uri
            prefLabel = LocalizedStrings().apply { en = "Document label" }
        }

        val licenseOne = UriWithLabelAndType().apply {
            uri = "https://license-one.com"
            extraType = DCTerms.LicenseDocument.uri
            prefLabel = LocalizedStrings().apply { en = "One" }
        }

        val licenseTwo = UriWithLabelAndType().apply {
            uri = "http://source.com"
            prefLabel = LocalizedStrings().apply { en = "Two" }
        }

        val expected = listOf(
            Distribution().apply {
                conformsTo = listOf(expectedConformsTo)
                page = listOf(expectedPage)
                license = listOf(licenseTwo, licenseOne)
            }
        )

        assertEquals(expected, subject.extractListOfDistributionsV1(DCAT.distribution))
    }

    @Test
    fun extractDistributionFormats() {
        val turtle = """
            @prefix dc:   <http://purl.org/dc/elements/1.1/> .
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                   dcat:Dataset ;
                dcat:distribution   [  a                dcat:Distribution ;
                                       dct:format        "csv" ;
                                       dct:format        <https://www.iana.org/assignments/media-types/text/csv> ;
                                       dct:format        <http://publications.europa.eu/resource/authority/file-type/CSV>
                                    ] .

            <https://www.iana.org/assignments/media-types/text/csv>
                a               dct:MediaType ;
                dct:identifier  "text/csv" ;
                dct:title       "csv" .

            <http://publications.europa.eu/resource/authority/file-type/CSV>
                a               <http://publications.europa.eu/ontology/euvoc#FileType> ;
                dc:identifier   "CSV" .
        """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected = listOf(
            Format().apply {
                name = "csv"
                code = "csv"
                type = FormatType.UNKNOWN
            },
            Format().apply {
                uri = "https://www.iana.org/assignments/media-types/text/csv"
                name = "csv"
                code = "text/csv"
                type = FormatType.MEDIA_TYPE
            },
            Format().apply {
                uri = "http://publications.europa.eu/resource/authority/file-type/CSV"
                name = "CSV"
                code = "CSV"
                type = FormatType.FILE_TYPE
            },
        )

        assertTrue(subject.extractListOfDistributionsV1(DCAT.distribution)!!.first().fdkFormat.containsAll(expected))
    }
}
