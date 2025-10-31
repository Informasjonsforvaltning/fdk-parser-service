package no.digdir.fdk.parserservice.extract.informationmodel

import no.digdir.fdk.model.ContactPoint
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.LosNode
import no.digdir.fdk.model.Organization
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.informationmodel.InformationModel
import no.digdir.fdk.model.informationmodel.InformationModelElement
import no.digdir.fdk.model.informationmodel.InformationModelProperty
import no.digdir.fdk.model.informationmodel.InformationModelStandard
import no.digdir.fdk.parserservice.parser.informationmodel.ModellDcatApNoV1Parser
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class InformationModelExtractionTest {
    @Test
    fun `should extract basic information model properties`() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .
            @prefix owl:   <http://www.w3.org/2002/07/owl#> .
            @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .

            <http://test.fellesdatakatalog.digdir.no/information-models/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <http://test.fellesdatakatalog.digdir.no/information-model/test> .

            <http://test.fellesdatakatalog.digdir.no/information-model/test>
                a                         modelldcatno:InformationModel ;
                dct:title                 "Test Informasjonsmodell"@no , "Test Information Model"@en ;
                dct:description           "Beskrivelse av informasjonsmodell"@no , "Description of information model"@en ;
                dct:publisher             <https://testdirektoratet.no/publisher> ;
                dct:identifier            "test-identifier" ;
                dct:issued                "2023-01-01"^^xsd:date ;
                dct:modified              "2023-01-02"^^xsd:date ;
                foaf:homepage             <https://example.com/info-model> ;
                dct:accessRights          <http://publications.europa.eu/resource/authority/access-right/PUBLIC> ;
                dct:language              <http://publications.europa.eu/resource/authority/language/NOB> ;
                dcat:keyword              "modell"@no , "model"@en ;
                modelldcatno:informationModelIdentifier "https://www.digdir.no/test-model" ;
                dct:type                  "Fellesmodell"@no ;
                owl:versionInfo           "1.0" ;
                dct:conformsTo            <https://statswiki.unece.org/display/gsim/Generic+Statistical+Information+Model> .

            <https://testdirektoratet.no/publisher>
                a                         foaf:Agent ;
                dct:identifier            "112233445" ;
                foaf:name                 "Test Publisher" .
            """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")

        val parser = ModellDcatApNoV1Parser()
        val informationModel =
            parser.parse(
                model,
                "http://test.fellesdatakatalog.digdir.no/information-model/test",
                "a1c680ca-62d7-34d5-aa4c-d39b5db033ae",
            )

        val expected =
            InformationModel().apply {
                id = "a1c680ca-62d7-34d5-aa4c-d39b5db033ae"
                uri = "http://test.fellesdatakatalog.digdir.no/information-model/test"
                type = ResourceType.informationmodels
                identifier = listOf("test-identifier")
                issued = "2023-01-01"
                modified = "2023-01-02"
                homepage = "https://example.com/info-model"
                language = listOf(ReferenceDataCode().apply { uri = "http://publications.europa.eu/resource/authority/language/NOB" })
                keyword = listOf(LocalizedStrings().apply { no = "modell" }, LocalizedStrings().apply { en = "model" })
                informationModelIdentifier = "https://www.digdir.no/test-model"
                conformsTo =
                    listOf(
                        InformationModelStandard().apply {
                            uri =
                                "https://statswiki.unece.org/display/gsim/Generic+Statistical+Information+Model"
                        },
                    )
                dctType = "Fellesmodell"
                versionInfo = "1.0"
                title =
                    LocalizedStrings().apply {
                        no = "Test Informasjonsmodell"
                        en = "Test Information Model"
                    }
                description =
                    LocalizedStrings().apply {
                        no = "Beskrivelse av informasjonsmodell"
                        en = "Description of information model"
                    }
                descriptionFormatted =
                    LocalizedStrings().apply {
                        no = "Beskrivelse av informasjonsmodell"
                        en = "Description of information model"
                    }
                publisher =
                    Organization().apply {
                        uri = "https://testdirektoratet.no/publisher"
                        id = "112233445"
                        prefLabel = LocalizedStrings().apply { no = "Test Publisher" }
                    }
            }

        assertEquals(expected, informationModel)
    }

    @Test
    fun `should extract themes`() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .

            <http://test.fellesdatakatalog.digdir.no/information-models/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <http://test.fellesdatakatalog.digdir.no/information-model/test> .

            <http://test.fellesdatakatalog.digdir.no/information-model/test>
                a                         modelldcatno:InformationModel ;
                dct:title                 "Test Information Model"@no ;
                dcat:theme                <https://psi.norge.no/los/tema/skole-og-utdanning> .

            <https://psi.norge.no/los/tema/skole-og-utdanning>
                a                         skos:Concept ;
                skos:prefLabel            "Skole og utdanning"@no .
            """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")

        val parser = ModellDcatApNoV1Parser()
        val informationModel =
            parser.parse(
                model,
                "http://test.fellesdatakatalog.digdir.no/information-model/test",
                "a1c680ca-62d7-34d5-aa4c-d39b5db033ae",
            )

        val expectedUris = listOf("https://psi.norge.no/los/tema/skole-og-utdanning")
        assertEquals(expectedUris, informationModel.themeUris.map { it.toString() })

        val expectedLosTheme =
            listOf(
                LosNode().apply {
                    uri = "https://psi.norge.no/los/tema/skole-og-utdanning"
                    code = "skole-og-utdanning"
                    isTema = true
                    name = LocalizedStrings().apply { no = "Skole og utdanning" }
                },
            )
        assertEquals(expectedLosTheme, informationModel.losTheme)
        assertNull(informationModel.theme)
        assertNull(informationModel.eurovocThemes)
    }

    @Test
    fun `should extract contact points`() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .
            @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .

            <http://test.fellesdatakatalog.digdir.no/information-models/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <http://test.fellesdatakatalog.digdir.no/information-model/test> .

            <http://test.fellesdatakatalog.digdir.no/information-model/test>
                a                         modelldcatno:InformationModel ;
                dct:title                 "Test Information Model"@no ;
                dcat:contactPoint         <http://test.fellesdatakatalog.digdir.no/information-model/test#contact> .

            <http://test.fellesdatakatalog.digdir.no/information-model/test#contact>
                a                         vcard:Organization ;
                vcard:fn                  "Test Contact"@en ;
                vcard:hasEmail            <mailto:test@example.com> ;
                vcard:hasTelephone        <tel:+4712345678> .
            """.trimIndent()

        val expected =
            listOf(
                ContactPoint().apply {
                    uri = "http://test.fellesdatakatalog.digdir.no/information-model/test#contact"
                    formattedName = LocalizedStrings().apply { en = "Test Contact" }
                    hasTelephone = "+4712345678"
                    email = "test@example.com"
                    fullname = "Test Contact"
                },
            )

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")

        val parser = ModellDcatApNoV1Parser()
        val informationModel =
            parser.parse(
                model,
                "http://test.fellesdatakatalog.digdir.no/information-model/test",
                "a1c680ca-62d7-34d5-aa4c-d39b5db033ae",
            )

        assertEquals(expected, informationModel.contactPoint)
    }

    @Test
    fun `should not add elements twice & handles circular references`() {
        val turtle =
            """
            @prefix owl:   <http://www.w3.org/2002/07/owl#> .
            @prefix ex-abstrakt: <http://example.com/test_abstraksjon#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .
            @prefix test: <https://test.com#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .

            <http://test.fellesdatakatalog.digdir.no/information-models/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://test.com#Diversemodell> .

            test:Diversemodell  a    modelldcatno:InformationModel , owl:NamedIndividual ;
                    modelldcatno:containsModelElement
                            test:Kjønn ;
                    modelldcatno:informationModelIdentifier
                            "https://www.digdir.no/diversemodell" .

            test:kjønn  a                      modelldcatno:Attribute ;
                    dct:identifier               "https://test.com#kjønn" ;
                    dct:title                    "kjønn"@nb ;
                    xsd:maxOccurs                "1"^^xsd:nonNegativeInteger ;
                    modelldcatno:hasValueFrom    test:Kjønn ;
                    modelldcatno:sequenceNumber  "3"^^xsd:positiveInteger .

            ex-abstrakt:EBU_EditorialObject
                    a                modelldcatno:ObjectType ;
                    dct:identifier   "http://example.com/test_abstraksjon#EBU_EditorialObject" ;
                    dct:title        "Is not added twice"@en ;
                    modelldcatno:hasProperty  test:kjønn .

            test:Kjønn  a         modelldcatno:CodeList ;
                    dct:identifier  "https://test.com#Kjønn" ; ;
                    modelldcatno:hasProperty  test:kjønn ;
                    dct:title       "Kjønn"@nb .
            """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")

        val expected =
            InformationModel().apply {
                id = "a1c680ca-62d7-34d5-aa4c-d39b5db033ae"
                uri = "https://test.com#Diversemodell"
                informationModelIdentifier = "https://www.digdir.no/diversemodell"
                containsModelElements = listOf("https://test.com#Kjønn")
                modelElements =
                    mapOf(
                        Pair(
                            "https://test.com#Kjønn",
                            InformationModelElement().apply {
                                uri = "https://test.com#Kjønn"
                                identifier = "https://test.com#Kjønn"
                                title = LocalizedStrings().apply { nb = "Kjønn" }
                                hasProperty = listOf("https://test.com#kjønn")
                                elementTypes = listOf("https://data.norge.no/vocabulary/modelldcatno#CodeList")
                            },
                        ),
                    )
                modelProperties =
                    mapOf(
                        Pair(
                            "https://test.com#kjønn",
                            InformationModelProperty().apply {
                                uri = "https://test.com#kjønn"
                                identifier = "https://test.com#kjønn"
                                title = LocalizedStrings().apply { nb = "kjønn" }
                                propertyTypes = listOf("https://data.norge.no/vocabulary/modelldcatno#Attribute")
                                sequenceNumber = 3
                                hasValueFrom = "https://test.com#Kjønn"
                            },
                        ),
                    )
                type = ResourceType.informationmodels
            }

        val parser = ModellDcatApNoV1Parser()
        val result =
            parser.parse(
                model,
                "https://test.com#Diversemodell",
                "a1c680ca-62d7-34d5-aa4c-d39b5db033ae",
            )

        assertEquals(expected, result)
    }

    @Test
    fun `should add subjects from elements and properties to contains subjects`() {
        val turtle =
            """
            @prefix owl:   <http://www.w3.org/2002/07/owl#> .
            @prefix ex-abstrakt: <http://example.com/test_abstraksjon#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .
            @prefix test: <https://test.com#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .

            <http://test.fellesdatakatalog.digdir.no/information-models/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://test.com#Diversemodell> .

            test:Diversemodell  a    modelldcatno:InformationModel ;
                    dct:subject             ex-abstrakt:begrep0 , ex-abstrakt:begrep1 ;
                    modelldcatno:containsModelElement
                            ex-abstrakt:Elm .

            ex-abstrakt:prop  a             modelldcatno:Attribute ;
                    dct:subject                   ex-abstrakt:begrep3 .

            ex-abstrakt:Elm  a              modelldcatno:ObjectType ;
                    modelldcatno:hasProperty      ex-abstrakt:prop ;
                    dct:subject                   ex-abstrakt:begrep2 .
            """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")

        val parser = ModellDcatApNoV1Parser()
        val result =
            parser.parse(
                model,
                "https://test.com#Diversemodell",
                "a1c680ca-62d7-34d5-aa4c-d39b5db033ae",
            )

        val expectedSubjects =
            listOf(
                "http://example.com/test_abstraksjon#begrep0",
                "http://example.com/test_abstraksjon#begrep1",
            )

        val expectedContainsSubjects =
            listOf(
                "http://example.com/test_abstraksjon#begrep0",
                "http://example.com/test_abstraksjon#begrep1",
                "http://example.com/test_abstraksjon#begrep2",
                "http://example.com/test_abstraksjon#begrep3",
            )

        assertEquals(expectedSubjects, result.subjects.map { it as String }.sorted())
        assertEquals(expectedContainsSubjects, result.containsSubjects.map { it as String }.sorted())
    }

    @Test
    fun `should add code element subjects to contains subjects`() {
        val turtle =
            """
            @prefix test: <https://test.com#> .
            @prefix adms:  <http://www.w3.org/ns/adms#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix owl:   <http://www.w3.org/2002/07/owl#> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
            @prefix xkos: <https://rdf-vocabulary.ddialliance.org/xkos/> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .

            <http://test.fellesdatakatalog.digdir.no/information-models/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <https://test.com#PersonOgEnhet> .

            test:PersonOgEnhet  a         modelldcatno:InformationModel ;
                    modelldcatno:containsModelElement test:Person ;
                    dct:identifier          "https://test.com#PersonOgEnhet"^^xsd:string .

            test:Person  a                     modelldcatno:ObjectType ;
                    modelldcatno:hasProperty       test:sivilstand .

            test:Sivilstand  a                 modelldcatno:CodeList ;
                    dct:subject        <http://begrepskatalogen/begrep/88804c58-ff43-11e6-9d97-005056825ca0> ;
                    dct:title              "Sivilstand"@nb .

            test:sivilstand  a             modelldcatno:Attribute ;
                    dct:subject <http://begrepskatalogen/begrep/88804c58-ff43-11e6-9d97-005056825ca0> ;
                    dct:title          "sivilstand"@nb ;
                    modelldcatno:hasValueFrom         test:Sivilstand ;
                    modelldcatno:sequenceNumber "11"^^xsd:positiveInteger ;
                    xsd:maxOccurs            "1"^^xsd:nonNegativeInteger .

            test:Ugift  a             modelldcatno:CodeElement ;
                    dct:identifier    "https://test.com#Ugift"^^xsd:string ;
                    skos:inScheme     test:Sivilstand ;
                    dct:subject       <http://begrepskatalogen/begrep/92f82e89-fb04-11e9-92b0-005056828ed3> ;
                    xkos:next         test:Gift ;
                    skos:topConceptOf test:Sivilstand ;
                    skos:prefLabel    "sivilstand ugift"@nb ;
                    skos:notation     "ugift"^^xsd:string .
            """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")

        val parser = ModellDcatApNoV1Parser()
        val result =
            parser.parse(
                model,
                "https://test.com#PersonOgEnhet",
                "a1c680ca-62d7-34d5-aa4c-d39b5db033ae",
            )

        val expectedContainsSubjects =
            listOf(
                "http://begrepskatalogen/begrep/88804c58-ff43-11e6-9d97-005056825ca0",
                "http://begrepskatalogen/begrep/92f82e89-fb04-11e9-92b0-005056828ed3",
            )
        assertEquals(
            expectedContainsSubjects,
            result.containsSubjects!!.map { it as String }.sorted(),
        )
    }
}
