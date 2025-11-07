package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.ContactPoint
import no.digdir.fdk.model.LocalizedStrings
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractContactPoint {
    @Test
    fun extractSingleContactPoint() {
        val turtle =
            """
            @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            
            <https://testdirektoratet.no/model/dataset/contact>
                    a                         dcat:Dataset ;
                    dcat:contactPoint
                        [ a                          vcard:Organization ;
                          vcard:hasTelephone         "23453345" ;
                          vcard:hasOrganizationName  "Testdirektoratet"@nb ;
                          vcard:hasURL               <https://testdirektoratet.no>
                        ] .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected =
            listOf(
                ContactPoint().apply {
                    organizationName = LocalizedStrings().apply { nb = "Testdirektoratet" }
                    hasURL = "https://testdirektoratet.no"
                    hasTelephone = "23453345"
                },
            )

        assertEquals(expected, subject.extractListOfContactPoints())
    }

    @Test
    fun extractMultipleContactPoints() {
        val turtle =
            """
            @prefix dct: <http://purl.org/dc/terms/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            
            <https://testdirektoratet.no/model/dataset/contact>
                    a                         dcat:Dataset ;
                    dcat:contactPoint
                        [ a                          vcard:Organization ;
                          vcard:hasEmail             "post@mail.com" ;
                          vcard:hasOrganizationName  "Testdirektoratet"@nb , "Testdirektoratet"@nn , "Directorate of test"@en ;
                          vcard:organization-unit    "Testenhet"@nb , "Testenhet"@nn , "Test unit"@en ;
                          vcard:hasURL               <https://testdirektoratet.no>
                        ] ;
                    dcat:contactPoint
                        <https://testdirektoratet.no/kontakt/testmann> ;
                    dcat:contactPoint
                        <https://testdirektoratet.no/kontakt/testmann2> .
            
            <https://testdirektoratet.no/kontakt/testmann>
                    a                          vcard:Organization ;
                    vcard:fn                   "Test Mann" ;
                    vcard:hasTelephone         <tel:12345678> ;
                    vcard:hasEmail             <mailto:testmann@mail.com> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected =
            listOf(
                ContactPoint().apply {
                    uri = "https://testdirektoratet.no/kontakt/testmann2"
                },
                ContactPoint().apply {
                    uri = "https://testdirektoratet.no/kontakt/testmann"
                    formattedName = LocalizedStrings().apply { no = "Test Mann" }
                    fullname = "Test Mann"
                    email = "testmann@mail.com"
                    hasTelephone = "12345678"
                },
                ContactPoint().apply {
                    email = "post@mail.com"
                    organizationName =
                        LocalizedStrings().apply {
                            nb = "Testdirektoratet"
                            nn = "Testdirektoratet"
                            en = "Directorate of test"
                        }
                    organizationUnit =
                        LocalizedStrings().apply {
                            nb = "Testenhet"
                            nn = "Testenhet"
                            en = "Test unit"
                        }
                    hasURL = "https://testdirektoratet.no"
                },
            )

        assertEquals(expected, subject.extractListOfContactPoints())
    }

    @Test
    fun extractContactPointWithEmailAndTelephoneAsNodes() {
        val turtle =
            """
            @prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            
            <https://testdirektoratet.no/model/dataset/contact>
                    a                         dcat:Dataset ;
                    dcat:contactPoint
                        [ a                          vcard:Organization ;
                          vcard:hasTelephone         <https://testdirektoratet.no/telephone> ;
                          vcard:hasEmail             [ a               vcard:Email ;
                                                       vcard:hasValue  "post@mail.com" ]
                        ] .
            
            <https://testdirektoratet.no/telephone>
                a               vcard:TelephoneType ;
                vcard:hasValue  <tel:99999999> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected =
            listOf(
                ContactPoint().apply {
                    email = "post@mail.com"
                    hasTelephone = "99999999"
                },
            )

        assertEquals(expected, subject.extractListOfContactPoints())
    }
}
