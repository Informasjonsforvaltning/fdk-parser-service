package no.digdir.fdk.parserservice.extract

import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals

@Tag("unit")
class ExtractReferenceDataCode {
    private val turtle =
        """
        @prefix dct:    <http://purl.org/dc/terms/> .
        @prefix dcat:   <http://www.w3.org/ns/dcat#> .

        <https://testdirektoratet.no/model/dataset/0>
            a           dcat:Dataset ;
            dct:type    <http://publications.europa.eu/resource/authority/dataset-type/TEST_DATA> ,
                        <https://data.norge.no/vocabulary/high-value-dataset-type#eu-hvd> .
        """.trimIndent()

    @Test
    fun codeFromUriIsIndependentOfSeparatorOrder() {
        val m = ModelFactory.createDefaultModel()
        m.read(StringReader(turtle), null, "TURTLE")
        val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

        val slashFirst = subject.extractListOfReferenceDataCodes(DCTerms.type, DC_11.identifier, listOf("/", "#"), SKOS.prefLabel)
        val hashFirst = subject.extractListOfReferenceDataCodes(DCTerms.type, DC_11.identifier, listOf("#", "/"), SKOS.prefLabel)

        assertEquals(setOf("TEST_DATA", "eu-hvd"), slashFirst?.map { it.code }?.toSet())
        assertEquals(setOf("TEST_DATA", "eu-hvd"), hashFirst?.map { it.code }?.toSet())
    }
}
