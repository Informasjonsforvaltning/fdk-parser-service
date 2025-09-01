package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.parser.dataset.DcatApNoV1Parser
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class DatasetExtractionTest {
    val parser = DcatApNoV1Parser()

    @Test
    fun extractsTitleAndDescriptionCorrectly() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset ;
                dct:title                 "title nb"@nb , "title nn"@nn , "title en"@en , "title no"@no ;
                dct:description           "description nb"@nb , "description nn"@nn , "description en"@en , "description no"@no .
        """.trimIndent()

        val expectedTitle = LocalizedStrings().also { title ->
            title.nb = "title nb"
            title.nn = "title nn"
            title.en = "title en"
            title.no = "title no"
        }

        val expectedDescription = LocalizedStrings().also { description ->
            description.nb = "description nb"
            description.nn = "description nn"
            description.en = "description en"
            description.no = "description no"
        }

        val expected = Dataset().also { dataset ->
            dataset.id = "a1c680ca-62d7-34d5-aa4c-d39b5db033ae"
            dataset.uri = "https://testdirektoratet.no/model/dataset/0"
            dataset.title = expectedTitle
            dataset.descriptionFormatted = expectedDescription
            dataset.description = expectedDescription
        }

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m)

        assertEquals(expected, result)
    }

    @Test
    fun descriptionExtractHandlesFormatting() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:Dataset ;
                dct:description           "<div>description</div>"@nb .
        """.trimIndent()

        val expectedFormattedDescription = LocalizedStrings().also { description ->
            description.nb = "<div>description</div>"
        }

        val expectedDescription = LocalizedStrings().also { description ->
            description.nb = "description"
        }

        val expected = Dataset().also { dataset ->
            dataset.id = "a1c680ca-62d7-34d5-aa4c-d39b5db033ae"
            dataset.uri = "https://testdirektoratet.no/model/dataset/0"
            dataset.descriptionFormatted = expectedFormattedDescription
            dataset.description = expectedDescription
        }

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m)

        assertEquals(expected, result)
    }

    @Test
    fun extractBooleansSuppliedByHarvestToFDK() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix fdk:   <https://raw.githubusercontent.com/Informasjonsforvaltning/fdk-reasoning-service/main/src/main/resources/ontology/fdk.owl#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                               dcat:Dataset ;
                fdk:isOpenData                  'true'^^xsd:boolean ;
                fdk:isAuthoritative             'true'^^xsd:boolean ;
                fdk:isRelatedToTransportportal  'true'^^xsd:boolean .
        """.trimIndent()

        val expected = Dataset().also { dataset ->
            dataset.id = "a1c680ca-62d7-34d5-aa4c-d39b5db033ae"
            dataset.uri = "https://testdirektoratet.no/model/dataset/0"
            dataset.isRelatedToTransportportal = true
            dataset.isAuthoritative = true
            dataset.isOpenData = true
        }

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m)

        assertEquals(expected, result)
    }

    @Test
    fun extractModifiedAndIssued() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                  dcat:Dataset ;
                dct:issued         "2018-01-11T10:50:10.111Z"^^xsd:dateTime ;
                dct:modified       "2020-02-22T12:52:20.222Z"^^xsd:dateTime .
        """.trimIndent()

        val expected = Dataset().also { dataset ->
            dataset.id = "a1c680ca-62d7-34d5-aa4c-d39b5db033ae"
            dataset.uri = "https://testdirektoratet.no/model/dataset/0"
            dataset.issued = "2018-01-11T10:50:10.111Z"
            dataset.modified = "2020-02-22T12:52:20.222Z"
        }

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m)

        assertEquals(expected, result)
    }

    @Test
    fun extractDctAndAdmsIdentifiers() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix adms:  <http://www.w3.org/ns/adms#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                  dcat:Dataset ;
                dct:identifier     <https://dct-uri.no> , "dct-string" ;
                adms:identifier    <https://adms-uri.no> , "adms-string" .
        """.trimIndent()

        val expectedDCT = listOf("dct-string", "https://dct-uri.no")
        val expectedADMS = listOf("adms-string", "https://adms-uri.no")

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m)

        assertEquals(expectedDCT, result.identifier?.map { it.toString() }?.sorted())
        assertEquals(expectedADMS, result.admsIdentifier?.map { it.toString() }?.sorted())
    }

    @Test
    fun extractPagesAndLandingPages() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                  dcat:Dataset ;
                foaf:page          <https://page0.no> , <https://page1.no> ;
                dcat:landingPage   <https://landing0.no> , <https://landing1.no> .
        """.trimIndent()

        val expectedPage = listOf("https://page0.no", "https://page1.no")
        val expectedLandingPage = listOf("https://landing0.no", "https://landing1.no")

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m)

        assertEquals(expectedPage, result.page?.map { it.toString() }?.sorted())
        assertEquals(expectedLandingPage, result.landingPage?.map { it.toString() }?.sorted())
    }

}
