package no.digdir.fdk.parserservice.parser.dataset

import no.digdir.fdk.parserservice.model.DcatProfile
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Tag("unit")
class DatasetProfileTest {
    private val datasetIRI = "https://testdirektoratet.no/model/dataset/0"

    private val prefixes =
        """
        @prefix dct:    <http://purl.org/dc/terms/> .
        @prefix dcat:   <http://www.w3.org/ns/dcat#> .
        @prefix dcatap: <http://data.europa.eu/r5r/> .
        @prefix mobilitydcatap: <https://w3id.org/mobilitydcat-ap#> .
        """.trimIndent()

    private fun modelOf(datasetTriples: String): Model {
        val model = ModelFactory.createDefaultModel()
        model.read(StringReader("$prefixes\n\n$datasetTriples"), null, "TURTLE")
        return model
    }

    @Test
    fun `mobility profile applies when the dataset has a mobility theme`() {
        val model =
            modelOf(
                """
                <https://testdirektoratet.no/model/dataset/0>
                    a                            dcat:Dataset ;
                    mobilitydcatap:mobilityTheme <https://w3id.org/mobilitydcat-ap/mobility-theme/bike-hiring-availability> .
                """.trimIndent(),
            )

        assertTrue(MobilityDcatApV3Parser().appliesTo(model, datasetIRI))
        assertEquals(DcatProfile.MOBILITY_DCAT_AP, MobilityDcatApV3Parser().dcatProfile())
    }

    @Test
    fun `mobility profile does not apply without a mobility theme`() {
        val model =
            modelOf(
                """
                <https://testdirektoratet.no/model/dataset/0>
                    a          dcat:Dataset ;
                    dct:title  "Datasett"@nb .
                """.trimIndent(),
            )

        assertFalse(MobilityDcatApV3Parser().appliesTo(model, datasetIRI))
    }

    @Test
    fun `hvd profile applies when the dataset has an hvd category`() {
        val model =
            modelOf(
                """
                <https://testdirektoratet.no/model/dataset/0>
                    a                  dcat:Dataset ;
                    dcatap:hvdCategory <http://data.europa.eu/bna/c_dd313021> .
                """.trimIndent(),
            )

        assertTrue(HvdDcatApNoParser().appliesTo(model, datasetIRI))
        assertEquals(DcatProfile.HVD_DCAT_AP_NO, HvdDcatApNoParser().dcatProfile())
    }

    @Test
    fun `hvd profile does not apply without an hvd category`() {
        val model =
            modelOf(
                """
                <https://testdirektoratet.no/model/dataset/0>
                    a          dcat:Dataset ;
                    dct:title  "Datasett"@nb .
                """.trimIndent(),
            )

        assertFalse(HvdDcatApNoParser().appliesTo(model, datasetIRI))
    }

    @Test
    fun `dcat-ap-no profile applies to every dataset`() {
        val model =
            modelOf(
                """
                <https://testdirektoratet.no/model/dataset/0>
                    a          dcat:Dataset ;
                    dct:title  "Datasett"@nb .
                """.trimIndent(),
            )

        listOf(DcatApNoV1Parser(), DcatApNoV2Parser(), DcatApNoV3Parser()).forEach { parser ->
            assertTrue(parser.appliesTo(model, datasetIRI))
            assertEquals(DcatProfile.DCAT_AP_NO, parser.dcatProfile())
        }
    }
}
