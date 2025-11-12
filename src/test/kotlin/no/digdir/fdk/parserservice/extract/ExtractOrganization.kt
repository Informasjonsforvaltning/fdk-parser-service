package no.digdir.fdk.parserservice.extract

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.Organization
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractOrganization {
    @Test
    fun extractPublisher() {
        val turtle =
            """
            @prefix fdkorg: <https://raw.githubusercontent.com/Informasjonsforvaltning/organization-catalog/main/src/main/resources/ontology/organization-catalog.owl#> .
            @prefix orgtype:   <https://raw.githubusercontent.com/Informasjonsforvaltning/organization-catalog/main/src/main/resources/ontology/org-type.ttl#> .
            @prefix dct:    <http://purl.org/dc/terms/> .
            @prefix dcat:   <http://www.w3.org/ns/dcat#> .
            @prefix foaf:   <http://xmlns.com/foaf/0.1/> .
            @prefix rov:    <http://www.w3.org/ns/regorg#> .

            <https://testdirektoratet.no/model/dataset/0>
                a                         dcat:DataService ;
                dct:publisher             <https://testdirektoratet.no/publisher> .

            <https://testdirektoratet.no/publisher>
                a               foaf:Agent ;
                dct:identifier  "112233445" ;
                rov:legalName   "TESTORGANISASJON" ;
                rov:orgType     orgtype:ORGL ;
                fdkorg:orgPath  "/STAT/987654321/123456789" ;
                foaf:name       "Norsk testorganisasjon"@nb .
            """.trimIndent()

        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.DataService).toList().first()

        val expected =
            Organization().also {
                it.uri = "https://testdirektoratet.no/publisher"
                it.id = "112233445"
                it.name = "Norsk testorganisasjon"
                it.orgPath = "/STAT/987654321/123456789"
                it.prefLabel = LocalizedStrings().also { label -> label.nb = "Norsk testorganisasjon" }
                it.title = LocalizedStrings().also { label -> label.nb = "Norsk testorganisasjon" }
            }

        assertEquals(expected, subject.extractOrganization(DCTerms.publisher))
    }
}
