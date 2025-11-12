package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.Organization
import no.digdir.fdk.model.dataset.QualifiedAttribution
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractDatasetQualifiedAttributions {
    @Test
    fun extractQualifiedAttributions() {
        val turtle =
            """
            @prefix dct:   <http://purl.org/dc/terms/> .
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix prov:  <http://www.w3.org/ns/prov#> .
            @prefix rov:   <http://www.w3.org/ns/regorg#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .

            <https://testdirektoratet.no/model/dataset/dataset-with-qualified-attributions>
                a                          dcat:Dataset ;
                prov:qualifiedAttribution  <https://testdirektoratet.no/qualified-attributions/0> ,
                                           <https://testdirektoratet.no/qualified-attributions/1> .

            <https://testdirektoratet.no/qualified-attributions/1>
                a             prov:Attribution ;
                dcat:hadRole  <http://registry.it.csiro.au/def/isotc211/CI_RoleCode/contributor> ;
                prov:agent    <https://data.brreg.no/enhetsregisteret/api/enheter/123456789> .

            <https://data.brreg.no/enhetsregisteret/api/enheter/123456789>
                a                      rov:RegisteredOrganization ;
                dct:identifier         "123456789" ;
                rov:legalName          "Testdirektoratet" ;
                foaf:name              "Norwegian Test Agency"@en .

            <https://testdirektoratet.no/qualified-attributions/0>
                a             prov:Attribution ;
                dcat:hadRole  <http://registry.it.csiro.au/def/isotc211/CI_RoleCode/contributor> ;
                prov:agent    <https://data.brreg.no/enhetsregisteret/api/enheter/987654321> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected =
            listOf(
                QualifiedAttribution().apply {
                    role = "http://registry.it.csiro.au/def/isotc211/CI_RoleCode/contributor"
                    agent =
                        Organization().apply {
                            uri = "https://data.brreg.no/enhetsregisteret/api/enheter/123456789"
                            id = "123456789"
                            name = "Testdirektoratet"
                            prefLabel = LocalizedStrings().apply { en = "Norwegian Test Agency" }
                            title = LocalizedStrings().apply { en = "Norwegian Test Agency" }
                        }
                },
                QualifiedAttribution().apply {
                    role = "http://registry.it.csiro.au/def/isotc211/CI_RoleCode/contributor"
                    agent =
                        Organization().apply {
                            uri = "https://data.brreg.no/enhetsregisteret/api/enheter/987654321"
                        }
                },
            )

        assertEquals(expected, subject.extractListOfQualifiedAttributions())
    }

    @Test
    fun extractQualifiedAttributionsHandlesMissingAgent() {
        val turtle =
            """
            @prefix dcat:  <http://www.w3.org/ns/dcat#> .
            @prefix prov:  <http://www.w3.org/ns/prov#> .

            <https://testdirektoratet.no/model/dataset/dataset-with-qualified-attributions>
                a                          dcat:Dataset ;
                prov:qualifiedAttribution  <https://testdirektoratet.no/qualified-attributions/0> .

            <https://testdirektoratet.no/qualified-attributions/0>
                a             prov:Attribution ;
                dcat:hadRole  <http://registry.it.csiro.au/def/isotc211/CI_RoleCode/contributor> .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val expected =
            listOf(
                QualifiedAttribution().apply {
                    role = "http://registry.it.csiro.au/def/isotc211/CI_RoleCode/contributor"
                },
            )

        assertEquals(expected, subject.extractListOfQualifiedAttributions())
    }
}
