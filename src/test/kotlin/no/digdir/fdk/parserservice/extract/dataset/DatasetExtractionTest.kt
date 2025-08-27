package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.EuDataTheme
import no.digdir.fdk.model.Eurovoc
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.LosNode
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.parser.dataset.DcatApNoV1Parser
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
            dataset.type = ResourceType.datasets
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
            dataset.type = ResourceType.datasets
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
            dataset.type = ResourceType.datasets
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
            dataset.type = ResourceType.datasets
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

    @Test
    fun extractKeywords() {
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
                dcat:keyword       "keyword0"@en , "keyword1"@en , "keyword2"@nn .
        """.trimIndent()

        val expectedKeywords = listOf(
            LocalizedStrings().also { it.en = "keyword0" },
            LocalizedStrings().also { it.en = "keyword1" },
            LocalizedStrings().also { it.nn = "keyword2" }
        )

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m)

        assertEquals(3, result.keyword.size)
        assertTrue { result.keyword.containsAll(expectedKeywords) }
    }

    @Test
    fun extractThemes() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                  dcat:Dataset ;
                dcat:theme         <http://publications.europa.eu/resource/authority/data-theme/TECH> ,
                                   <http://eurovoc.europa.eu/1338> , <https://psi.norge.no/los/tema/naring> .

            <http://publications.europa.eu/resource/authority/data-theme/TECH>
                skos:prefLabel	"Vitskap og teknologi"@nn ;
                skos:prefLabel	"Science and technology"@en ;
                skos:prefLabel	"Vitenskap og teknologi"@nb ;
                skos:prefLabel	"Vitenskap og teknologi"@no .

            <http://eurovoc.europa.eu/1338>
                skos:prefLabel  "India"@en ;
                <https://fellesdatakatalog.digdir.no/ontology/internal/themePath> "8367/1338" .

            <https://psi.norge.no/los/tema/naring>
                skos:prefLabel     "Business"@en , "Næring"@nb , "Næring"@nn ;
                <https://fellesdatakatalog.digdir.no/ontology/internal/themePath> "naring" .
        """.trimIndent()

        val expectedUris = listOf(
            "http://eurovoc.europa.eu/1338",
            "http://publications.europa.eu/resource/authority/data-theme/TECH",
            "https://psi.norge.no/los/tema/naring"
        )
        val expectedDataTheme = listOf(EuDataTheme().also { theme ->
            theme.uri = "http://publications.europa.eu/resource/authority/data-theme/TECH"
            theme.code = "TECH"
            theme.title = LocalizedStrings().also { title ->
                title.no = "Vitenskap og teknologi"
                title.nb = "Vitenskap og teknologi"
                title.nn = "Vitskap og teknologi"
                title.en = "Science and technology"
            }
        })
        val expectedLos = listOf(LosNode().also { theme ->
            theme.uri = "https://psi.norge.no/los/tema/naring"
            theme.code = "naring"
            theme.losPaths = listOf("naring")
            theme.isTema = true
            theme.name = LocalizedStrings().also { name ->
                name.nb = "Næring"
                name.nn = "Næring"
                name.en = "Business"
            }
        })
        val expectedEurovoc = listOf(Eurovoc().also { theme ->
            theme.uri = "http://eurovoc.europa.eu/1338"
            theme.code = "1338"
            theme.eurovocPaths = listOf("8367/1338")
            theme.label = LocalizedStrings().also { label ->
                label.en = "India"
            }
        })

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m)

        assertEquals(expectedUris, result.themeUris?.map { it.toString() }?.sorted())
        assertEquals(expectedDataTheme, result.theme)
        assertEquals(expectedLos, result.losTheme)
        assertEquals(expectedEurovoc, result.eurovocThemes)
    }

    @Test
    fun extractTypeFrequencyProvenanceAndSpatial() {
        val turtle = """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix skos:   <http://www.w3.org/2004/02/skos/core#> .
            @prefix at:    <http://publications.europa.eu/ontology/authority/> .
            @prefix dc:   <http://purl.org/dc/elements/1.1/> .

            <http://test.fellesdatakatalog.digdir.no/datasets/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://testdirektoratet.no/model/dataset/0> .

            <https://testdirektoratet.no/model/dataset/0>
                a                      dcat:Dataset ;
                dct:spatial            <https://data.geonorge.no/administrativeEnheter/fylke/id/34> ;
                dct:provenance         <http://data.brreg.no/datakatalog/provinens/nasjonal> ;
                dct:accrualPeriodicity <http://publications.europa.eu/resource/authority/frequency/ANNUAL> ;
                dct:type               <http://publications.europa.eu/resource/authority/dataset-type/TEST_DATA> .

            <https://data.geonorge.no/administrativeEnheter/fylke/id/34>
                a               dct:Location;
                dct:identifier  "34";
                dct:title       "Innlandet" .

            <http://data.brreg.no/datakatalog/provinens/nasjonal>
                a                  skos:Concept;
                at:authority-code      "NASJONAL";
                skos:prefLabel     "Autoritativ kilde"@nn , "Autoritativ kilde"@nb , "Authoritativ source"@en .

            <http://publications.europa.eu/resource/authority/frequency/ANNUAL>
                dc:identifier  "ANNUAL";
                skos:prefLabel  "annual"@en , "årleg"@nn , "årlig"@nb , "årlig"@no .

            <http://publications.europa.eu/resource/authority/dataset-type/TEST_DATA>
                dc:identifier   "TEST_DATA" ;
                skos:prefLabel  "Testdata"@nn , "Test data"@en , "Testdata"@no , "Testdata"@nb .
        """.trimIndent()

        val expectedSpatial = listOf(ReferenceDataCode().apply {
            uri = "https://data.geonorge.no/administrativeEnheter/fylke/id/34"
            code = "34"
            prefLabel = LocalizedStrings().apply {
                nb = "Innlandet"
            }
        })

        val expectedProvenance = ReferenceDataCode().apply {
            uri = "http://data.brreg.no/datakatalog/provinens/nasjonal"
            code = "NASJONAL"
            prefLabel = LocalizedStrings().apply {
                nb = "Autoritativ kilde"
                nn = "Autoritativ kilde"
                en = "Authoritativ source"
            }
        }

        val expectedFrequency = ReferenceDataCode().apply {
            uri = "http://publications.europa.eu/resource/authority/frequency/ANNUAL"
            code = "ANNUAL"
            prefLabel = LocalizedStrings().apply {
                no = "årlig"
                nb = "årlig"
                nn = "årleg"
                en = "annual"
            }
        }

        val expectedType = ReferenceDataCode().apply {
            uri = "http://publications.europa.eu/resource/authority/dataset-type/TEST_DATA"
            code = "TEST_DATA"
            prefLabel = LocalizedStrings().apply {
                no = "Testdata"
                nb = "Testdata"
                nn = "Testdata"
                en = "Test data"
            }
        }

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val result = parser.parse(m)

        assertEquals(expectedSpatial, result.spatial)
        assertEquals(expectedProvenance, result.provenance)
        assertEquals(expectedFrequency, result.accrualPeriodicity)
        assertEquals(expectedType, result.dctType)
    }

}
