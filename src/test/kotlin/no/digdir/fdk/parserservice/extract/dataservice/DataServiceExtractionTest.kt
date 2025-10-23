package no.digdir.fdk.parserservice.extract.dataservice

import no.digdir.fdk.model.ContactPoint
import no.digdir.fdk.model.EuDataTheme
import no.digdir.fdk.model.Format
import no.digdir.fdk.model.FormatType
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.LosNode
import no.digdir.fdk.model.Organization
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.UriWithLabel
import no.digdir.fdk.model.dataservice.DataService
import no.digdir.fdk.parserservice.parser.dataservice.DcatApNoV2Parser
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class DataServiceExtractionTest {

    @Test
    fun `should extract basic data service properties`() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            <http://test.fellesdatakatalog.digdir.no/data-services/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <http://test.fellesdatakatalog.digdir.no/data-service/test> .

            <http://test.fellesdatakatalog.digdir.no/data-service/test>
                a                         dcat:DataService ;
                dct:title                 "Test datatjeneste"@no , "Test Data Service"@en ;
                dct:description           "Beskrivelse av datatjeneste"@no , "Description of data service"@en ;
                dct:publisher             <https://testdirektoratet.no/publisher> ;
                dct:identifier            "test-identifier" ;
                dct:issued                "2023-01-01"^^xsd:date ;
                dct:modified              "2023-01-02"^^xsd:date ;
                dcat:landingPage          <https://example.com/landing> ;
                foaf:page                 <https://example.com/page> ;
                dct:accessRights          <http://publications.europa.eu/resource/authority/access-right/PUBLIC> ;
                dct:language              <http://publications.europa.eu/resource/authority/language/NOB> ;
                dcat:keyword              "keyword"@en ;
                dct:conformsTo             <https://api.example.com/spec> ;
                dct:type                  "API" ;
                dcat:endpointURL          <https://api.example.com/data> ;
                dcat:endpointDescription  <https://api.example.com/docs> ;
                dcat:servesDataset        <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/publisher>
                a                         foaf:Agent ;
                dct:identifier            "112233445" ;
                foaf:name                 "Test Publisher" .
        """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")

        val parser = DcatApNoV2Parser()
        val dataService = parser.parse(model, "http://test.fellesdatakatalog.digdir.no/data-service/test", "a1c680ca-62d7-34d5-aa4c-d39b5db033ae")

        val expected = DataService().apply {
            id = "a1c680ca-62d7-34d5-aa4c-d39b5db033ae"
            uri = "http://test.fellesdatakatalog.digdir.no/data-service/test"
            type = ResourceType.dataservices
            identifier = listOf("test-identifier")
            issued = "2023-01-01"
            modified = "2023-01-02"
            landingPage = listOf("https://example.com/landing")
            page = listOf("https://example.com/page")
            accessRights = ReferenceDataCode().apply { uri = "http://publications.europa.eu/resource/authority/access-right/PUBLIC" }
            language = listOf(ReferenceDataCode().apply { uri = "http://publications.europa.eu/resource/authority/language/NOB" })
            keyword = listOf(LocalizedStrings().apply { en = "keyword" })
            conformsTo = listOf(UriWithLabel().apply { uri = "https://api.example.com/spec" })
            dctType = "API"
            endpointURL = listOf("https://api.example.com/data")
            endpointDescription = listOf("https://api.example.com/docs")
            servesDataset = listOf("https://testdirektoratet.no/model/dataset/0")
            title = LocalizedStrings().apply {
                no = "Test datatjeneste"
                en = "Test Data Service"
            }
            description = LocalizedStrings().apply {
                no = "Beskrivelse av datatjeneste"
                en = "Description of data service"
            }
            descriptionFormatted = LocalizedStrings().apply {
                no = "Beskrivelse av datatjeneste"
                en = "Description of data service"
            }
            publisher = Organization().apply {
                uri = "https://testdirektoratet.no/publisher"
                id = "112233445"
                prefLabel = LocalizedStrings().apply { no = "Test Publisher" }
            }
        }

        assertEquals(expected, dataService)
    }

    @Test
    fun `should extract themes`() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

            <http://test.fellesdatakatalog.digdir.no/data-services/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <http://test.fellesdatakatalog.digdir.no/data-service/test> .

            <http://test.fellesdatakatalog.digdir.no/data-service/test>
                a                         dcat:DataService ;
                dct:title                 "Test Data Service"@no ;
                dcat:theme                <http://publications.europa.eu/resource/authority/data-theme/TRANSP> ;
                dcat:theme                <https://psi.norge.no/los/tema/transport> .

            <http://publications.europa.eu/resource/authority/data-theme/TRANSP>
                a                         skos:Concept ;
                skos:prefLabel            "Transport"@en .

            <https://psi.norge.no/los/tema/transport>
                a                         skos:Concept ;
                skos:prefLabel            "Transport"@no .
        """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")

        val parser = DcatApNoV2Parser()
        val dataService = parser.parse(model, "http://test.fellesdatakatalog.digdir.no/data-service/test", "a1c680ca-62d7-34d5-aa4c-d39b5db033ae")

        val expectedUris = listOf("http://publications.europa.eu/resource/authority/data-theme/TRANSP", "https://psi.norge.no/los/tema/transport")
        assertEquals(expectedUris, dataService.themeUris.map { it.toString() }.sorted())

        val expectedDataTheme = listOf(EuDataTheme().apply {
            uri = "http://publications.europa.eu/resource/authority/data-theme/TRANSP"
            code = "TRANSP"
            title = LocalizedStrings().apply { en = "Transport" }
        })
        assertEquals(expectedDataTheme, dataService.theme)

        val expectedLosTheme = listOf(LosNode().apply {
            uri = "https://psi.norge.no/los/tema/transport"
            code = "transport"
            isTema = true
            name = LocalizedStrings().apply { no = "Transport" }
        })
        assertEquals(expectedLosTheme, dataService.losTheme)
    }

    @Test
    fun `should extract contact points`() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

            <http://test.fellesdatakatalog.digdir.no/data-services/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <http://test.fellesdatakatalog.digdir.no/data-service/test> .

            <http://test.fellesdatakatalog.digdir.no/data-service/test>
                a                         dcat:DataService ;
                dct:title                 "Test Data Service"@no ;
                dcat:contactPoint         <http://test.fellesdatakatalog.digdir.no/data-service/test#contact> .

            <http://test.fellesdatakatalog.digdir.no/data-service/test#contact>
                a                         vcard:Organization ;
                vcard:fn                  "Test Contact" ;
                vcard:hasEmail            <mailto:test@example.com> ;
                vcard:hasTelephone        <tel:+4712345678> .
        """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")

        val parser = DcatApNoV2Parser()
        val dataService = parser.parse(model, "http://test.fellesdatakatalog.digdir.no/data-service/test", "a1c680ca-62d7-34d5-aa4c-d39b5db033ae")

        val expected = listOf(ContactPoint().apply {
            uri = "http://test.fellesdatakatalog.digdir.no/data-service/test#contact"
            fullname = "Test Contact"
            email = "test@example.com"
            hasTelephone = "+4712345678"
        })
        assertEquals(expected, dataService.contactPoint)
    }

    @Test
    fun `should extract formats`() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

            <http://test.fellesdatakatalog.digdir.no/data-services/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <http://test.fellesdatakatalog.digdir.no/data-service/test> .

            <http://test.fellesdatakatalog.digdir.no/data-service/test>
                a                         dcat:DataService ;
                dct:title                 "Test Data Service"@no ;
                dct:format                <http://publications.europa.eu/resource/authority/file-type/JSON> .

            <http://publications.europa.eu/resource/authority/file-type/JSON>
                a                         dct:MediaType ;
                dct:title                 "JSON"@en ;
                dct:identifier            "JSON" .
        """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")

        val parser = DcatApNoV2Parser()
        val dataService = parser.parse(model, "http://test.fellesdatakatalog.digdir.no/data-service/test", "a1c680ca-62d7-34d5-aa4c-d39b5db033ae")

        val expected = listOf(
            Format().apply {
                uri = "http://publications.europa.eu/resource/authority/file-type/JSON"
                name = "JSON"
                code = "JSON"
                type = FormatType.MEDIA_TYPE
            }
        )

        assertEquals(expected, dataService.fdkFormat)
    }
}
