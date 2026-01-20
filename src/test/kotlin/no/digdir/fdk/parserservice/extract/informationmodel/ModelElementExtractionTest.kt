package no.digdir.fdk.parserservice.extract.informationmodel

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.informationmodel.InformationModelCodeElement
import no.digdir.fdk.model.informationmodel.InformationModelElement
import no.digdir.fdk.parserservice.parser.informationmodel.ModellDcatApNoV1Parser
import no.digdir.fdk.parserservice.vocabulary.MODELLDCATNO
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("unit")
class ModelElementExtractionTest {
    @Test
    fun `should extract information model with model elements`() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .
            @prefix owl:   <http://www.w3.org/2002/07/owl#> .

            <http://test.fellesdatakatalog.digdir.no/information-models/a1c680ca-62d7-34d5-aa4c-d39b5db033ae>
                a                  dcat:CatalogRecord ;
                dct:identifier     "a1c680ca-62d7-34d5-aa4c-d39b5db033ae" ;
                foaf:primaryTopic  <http://test.fellesdatakatalog.digdir.no/information-model/test> .

            <http://test.fellesdatakatalog.digdir.no/information-model/test>
                a                         modelldcatno:InformationModel ;
                dct:title                 "Test Information Model"@no ;
                modelldcatno:containsModelElement <http://test.fellesdatakatalog.digdir.no/information-model/test#element> .

            <http://test.fellesdatakatalog.digdir.no/information-model/test#element>
                a                         modelldcatno:ObjectType ;
                dct:title                 "Test Element"@no ;
                dct:identifier            "test-element-id" ;
                dct:description           "Beskrivelse av element"@no .
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

        val expectedKey = "http://test.fellesdatakatalog.digdir.no/information-model/test#element"
        val expectedValue =
            InformationModelElement().apply {
                uri = expectedKey
                identifier = "test-element-id"
                title = LocalizedStrings().apply { no = "Test Element" }
                description = LocalizedStrings().apply { no = "Beskrivelse av element" }
                elementTypes = listOf("https://data.norge.no/vocabulary/modelldcatno#ObjectType")
            }

        assertEquals(listOf(expectedKey), informationModel.containsModelElements)
        assertEquals(mapOf(Pair(expectedKey as CharSequence, expectedValue)), informationModel.modelElements)
    }

    @Test
    fun `should extract simple type element`() {
        val turtle =
            """
            @prefix owl:   <http://www.w3.org/2002/07/owl#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .
            @prefix testdir: <https://testdirektoratet.no/model#> .

            testdir:SomeTextType
                a                   modelldcatno:SimpleType ;
                modelldcatno:subject    "http://data.brreg.no/begrep/28155"^^xsd:anyURI ;
                dct:identifier  "SomeTextType"^^xsd:string ;
                dct:title   "Some text"@en ;
                modelldcatno:typeDefinitionReference    "http://www.w3.org/2001/XMLSchema#string"^^xsd:anyURI ;
                xsd:length  "9"^^xsd:nonNegativeInteger ;
                xsd:maxLength   "9"^^xsd:nonNegativeInteger ;
                xsd:minLength   "9"^^xsd:nonNegativeInteger ;
                xsd:pattern     "[0-9]+"^^xsd:string .
            """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")
        val subject = model.listSubjectsWithProperty(RDF.type, MODELLDCATNO.SimpleType).toList().first()

        val expected =
            InformationModelElement().apply {
                uri = "https://testdirektoratet.no/model#SomeTextType"
                identifier = "SomeTextType"
                title = LocalizedStrings().apply { en = "Some text" }
                elementTypes = listOf("https://data.norge.no/vocabulary/modelldcatno#SimpleType")
                typeDefinitionReference = "http://www.w3.org/2001/XMLSchema#string"
                length = 9
                maxLength = 9
                minLength = 9
                pattern = "[0-9]+"
            }

        assertEquals(expected, subject.buildModelElement())
    }

    @Test
    fun `should extract code list element`() {
        val turtle =
            """
            @prefix owl:   <http://www.w3.org/2002/07/owl#> .
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .
            @prefix testdir: <https://testdirektoratet.no/model#> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix xkos:  <http://rdf-vocabulary.ddialliance.org/xkos#> .

            testdir:Kjønn  a         modelldcatno:CodeList ;
                dct:identifier  "Kjønn" ;
                dct:title       "Kjønn"@nb .

            testdir:Kvinne  a         modelldcatno:CodeElement ;
                dct:identifier  "Kvinne"^^xsd:string ;
                skos:inScheme   testdir:Kjønn ;
                skos:topConceptOf testdir:Kjønn ;
                xkos:next         testdir:Mann ;
                skos:prefLabel  "kvinne"@nb .

            testdir:Mann  a           modelldcatno:CodeElement ;
                dct:identifier  "Mann"^^xsd:string ;
                skos:inScheme   testdir:Kjønn ;
                skos:prefLabel  "mann"@nb .
            """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")
        val subject = model.listSubjectsWithProperty(RDF.type, MODELLDCATNO.CodeList).toList().first()

        val expected =
            InformationModelElement().apply {
                uri = "https://testdirektoratet.no/model#Kjønn"
                identifier = "Kjønn"
                title = LocalizedStrings().apply { nb = "Kjønn" }
                elementTypes = listOf("https://data.norge.no/vocabulary/modelldcatno#CodeList")
                codes =
                    listOf(
                        InformationModelCodeElement().apply {
                            uri = "https://testdirektoratet.no/model#Mann"
                            identifier = "Mann"
                            prefLabel = LocalizedStrings().apply { nb = "mann" }
                            inScheme = listOf("https://testdirektoratet.no/model#Kjønn")
                        },
                        InformationModelCodeElement().apply {
                            uri = "https://testdirektoratet.no/model#Kvinne"
                            identifier = "Kvinne"
                            prefLabel = LocalizedStrings().apply { nb = "kvinne" }
                            inScheme = listOf("https://testdirektoratet.no/model#Kjønn")
                            topConceptOf = listOf("https://testdirektoratet.no/model#Kjønn")
                            nextElement = listOf("https://testdirektoratet.no/model#Mann")
                        },
                    )
            }

        assertEquals(expected, subject.buildModelElement())
    }

    @Test
    fun `should extract null when information model element is missing all fields`() {
        val turtle =
            """
            <https://test.com#Model/.well-known/skolem/123>
                <https://test.com#randomPredicate>  "Lorem ipsum" .
            """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")
        val element = model.getResource("https://test.com#Model/.well-known/skolem/123")

        assertNull(element.buildModelElement())
    }

    @Test
    fun `should ignore code element with no values when extracting code list element`() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
            @prefix xkos:  <https://rdf-vocabulary.ddialliance.org/xkos/> .
            @prefix modelldcatno: <https://data.norge.no/vocabulary/modelldcatno#> .
            @prefix test: <https://test.com#> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .

            test:CodeList
                a                         modelldcatno:CodeList ;
                dct:identifier            "code-list" ;
                dct:title                 "Code List"@no .

            <https://test.com#Model/.well-known/skolem/123>
                a           modelldcatno:CodeElement ;
                skos:inScheme             test:CodeList .
            """.trimIndent()

        val model = ModelFactory.createDefaultModel()
        model.read(StringReader(turtle), null, "TURTLE")
        val codeList = model.getResource("https://test.com#CodeList")

        val expected =
            InformationModelElement().apply {
                uri = "https://test.com#CodeList"
                identifier = "code-list"
                title = LocalizedStrings().apply { no = "Code List" }
                elementTypes = listOf("https://data.norge.no/vocabulary/modelldcatno#CodeList")
                codes = null
            }

        assertEquals(expected, codeList.buildModelElement())
    }
}
