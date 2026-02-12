package no.digdir.fdk.parserservice.extract.dataset

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.dataset.QualityAnnotation
import no.digdir.fdk.parserservice.vocabulary.DQVISO
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class ExtractDatasetQualityAnnotation {
    @Nested
    internal inner class V2 {
        @Test
        fun extractRelevanceAnnotation() {
            val turtle =
                """
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix dqv:   <http://www.w3.org/ns/dqv#> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct: <http://purl.org/dc/terms/> .
                @prefix oa:    <http://www.w3.org/ns/oa#> .

                    <https://testdirektoratet.no/model/dataset/0>
                        a               dcat:Dataset ;
                        dqv:hasQualityAnnotation  <https://testdirektoratet.no/relevance> .

                    <https://testdirektoratet.no/relevance>
                        a                dqv:QualityAnnotation ;
                        dqv:inDimension  <http://iso.org/25012/2008/dataquality/Relevance> ;
                        oa:hasBody       [
                            a               oa:TextualBody ;
                            rdf:value       "relevans" ;
                            dct:language     <http://publications.europa.eu/resource/authority/language/NNO>
                        ] .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

            val expected =
                QualityAnnotation().apply {
                    inDimension = DQVISO.Relevance.uri
                    qualityDimensions = listOf(ReferenceDataCode().apply { uri = DQVISO.Relevance.uri })
                    hasBody = LocalizedStrings().apply { nn = "relevans" }
                }

            assertEquals(expected, subject.extractQualityAnnotationV2(DQVISO.Relevance))
        }

        @Test
        fun extractCurrentnessAnnotation() {
            val turtle =
                """
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix dqv:   <http://www.w3.org/ns/dqv#> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct: <http://purl.org/dc/terms/> .
                @prefix oa:    <http://www.w3.org/ns/oa#> .

                    <https://testdirektoratet.no/model/dataset/0>
                        a               dcat:Dataset ;
                        dqv:hasQualityAnnotation  <https://testdirektoratet.no/currentness> .

                    <https://testdirektoratet.no/currentness>
                        a                dqv:QualityAnnotation ;
                        dqv:inDimension
                            <http://iso.org/25012/2008/dataquality/Currentness> ;
                        oa:hasBody     [
                            a               oa:TextualBody ;
                            rdf:value       "currentness" ;
                            dct:language     <http://publications.europa.eu/resource/authority/language/ENG>
                        ] .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

            val expected =
                QualityAnnotation().apply {
                    inDimension = DQVISO.Currentness.uri
                    qualityDimensions = listOf(ReferenceDataCode().apply { uri = DQVISO.Currentness.uri })
                    hasBody = LocalizedStrings().apply { en = "currentness" }
                }

            assertEquals(expected, subject.extractQualityAnnotationV2(DQVISO.Currentness))
        }

        @Test
        fun extractAvailabilityAnnotation() {
            val turtle =
                """
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix dqv:   <http://www.w3.org/ns/dqv#> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct: <http://purl.org/dc/terms/> .
                @prefix oa:    <http://www.w3.org/ns/oa#> .

                    <https://testdirektoratet.no/model/dataset/0>
                        a               dcat:Dataset ;
                        dqv:hasQualityAnnotation  <https://testdirektoratet.no/availability> .

                    <https://testdirektoratet.no/availability>
                        a                dqv:QualityAnnotation ;
                        dqv:inDimension
                            <http://iso.org/25012/2008/dataquality/Availability> ;
                        oa:hasBody     [
                            a    oa:TextualBody ;
                            rdf:value  "availability"
                        ] .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

            val expected =
                QualityAnnotation().apply {
                    inDimension = DQVISO.Availability.uri
                    qualityDimensions = listOf(ReferenceDataCode().apply { uri = DQVISO.Availability.uri })
                    hasBody = LocalizedStrings().apply { no = "availability" }
                }

            assertEquals(expected, subject.extractQualityAnnotationV2(DQVISO.Availability))
        }

        @Test
        fun extractAccuracyAnnotation() {
            val turtle =
                """
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix dqv:   <http://www.w3.org/ns/dqv#> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct: <http://purl.org/dc/terms/> .
                @prefix oa:    <http://www.w3.org/ns/oa#> .

                    <https://testdirektoratet.no/model/dataset/0>
                        a               dcat:Dataset ;
                        dqv:hasQualityAnnotation  <https://testdirektoratet.no/accuracy> .

                    <https://testdirektoratet.no/accuracy>
                        a                dqv:QualityAnnotation ;
                        dqv:inDimension
                            <http://iso.org/25012/2008/dataquality/Accuracy> ;
                        oa:hasBody     [ a    oa:TextualBody ;
                            rdf:value  "Accuracy" ;
                            dct:language     <http://publications.europa.eu/resource/authority/language/ENG> ;
                            dct:format       <http://publications.europa.eu/resource/authority/file-type/TXT> ] .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

            val expected =
                QualityAnnotation().apply {
                    inDimension = DQVISO.Accuracy.uri
                    qualityDimensions = listOf(ReferenceDataCode().apply { uri = DQVISO.Accuracy.uri })
                    hasBody = LocalizedStrings().apply { en = "Accuracy" }
                }

            assertEquals(expected, subject.extractQualityAnnotationV2(DQVISO.Accuracy))
        }

        @Test
        fun extractCompletenessAnnotation() {
            val turtle =
                """
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix dqv:   <http://www.w3.org/ns/dqv#> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct: <http://purl.org/dc/terms/> .
                @prefix oa:    <http://www.w3.org/ns/oa#> .

                    <https://testdirektoratet.no/model/dataset/0>
                        a               dcat:Dataset ;
                        dqv:hasQualityAnnotation  <https://testdirektoratet.no/completeness> .

                    <https://testdirektoratet.no/completeness>
                        a                dqv:QualityAnnotation ;
                        dqv:inDimension  <http://iso.org/25012/2008/dataquality/Completeness> ;
                        oa:hasBody     [
                            rdf:value  "Completeness" ;
                            dct:language     <http://publications.europa.eu/resource/authority/language/NOB> ;
                            dct:format       <http://publications.europa.eu/resource/authority/file-type/TXT> ] .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

            val expected =
                QualityAnnotation().apply {
                    inDimension = DQVISO.Completeness.uri
                    qualityDimensions = listOf(ReferenceDataCode().apply { uri = DQVISO.Completeness.uri })
                    hasBody = LocalizedStrings().apply { nb = "Completeness" }
                }

            assertEquals(expected, subject.extractQualityAnnotationV2(DQVISO.Completeness))
        }

        @Test
        fun extractIgnoresAnnotationWithNoBody() {
            val turtle =
                """
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix dqv:   <http://www.w3.org/ns/dqv#> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct: <http://purl.org/dc/terms/> .
                @prefix oa:    <http://www.w3.org/ns/oa#> .

                    <https://testdirektoratet.no/model/dataset/0>
                        a               dcat:Dataset ;
                        dqv:hasQualityAnnotation  <https://testdirektoratet.no/completeness> .

                    <https://testdirektoratet.no/completeness>
                        a                dqv:QualityAnnotation ;
                        dqv:inDimension  <http://iso.org/25012/2008/dataquality/Completeness> .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

            assertNull(subject.extractQualityAnnotationV2(DQVISO.Completeness))
        }
    }

    @Nested
    internal inner class V3 {
        @Test
        fun extractSingleQualityAnnotation() {
            val turtle =
                """
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix dqv:   <http://www.w3.org/ns/dqv#> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix oa:    <http://www.w3.org/ns/oa#> .

                <https://testdirektoratet.no/model/dataset/0>
                    a dcat:Dataset ;
                    dqv:hasQualityAnnotation <https://testdirektoratet.no/quality/relevance> .

                <https://testdirektoratet.no/quality/relevance>
                    a dqv:QualityAnnotation ;
                    dqv:inDimension <http://iso.org/25012/2008/dataquality/Relevance> ;
                    oa:hasBody [
                        a oa:TextualBody ;
                        rdf:value "This dataset is highly relevant" ;
                        dct:language <http://publications.europa.eu/resource/authority/language/ENG>
                    ] .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

            val result = subject.extractListOfQualityAnnotations()

            val expected =
                listOf(
                    QualityAnnotation().apply {
                        hasBody = LocalizedStrings().apply { en = "This dataset is highly relevant" }
                        inDimension = "http://iso.org/25012/2008/dataquality/Relevance"
                        qualityDimensions =
                            listOf(
                                ReferenceDataCode().apply {
                                    uri = "http://iso.org/25012/2008/dataquality/Relevance"
                                    code = "http://iso.org/25012/2008/dataquality/Relevance"
                                },
                            )
                    },
                )

            assertEquals(expected, result)
        }

        @Test
        fun extractMultipleQualityAnnotations() {
            val turtle =
                """
                @prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix dqv:   <http://www.w3.org/ns/dqv#> .
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct:   <http://purl.org/dc/terms/> .
                @prefix oa:    <http://www.w3.org/ns/oa#> .
                @prefix skos:  <http://www.w3.org/2004/02/skos/core#> .

                <https://testdirektoratet.no/model/dataset/0>
                    a dcat:Dataset ;
                    dqv:hasQualityAnnotation <https://testdirektoratet.no/quality/relevance> ,
                                            <https://testdirektoratet.no/quality/currentness> ,
                                            <https://testdirektoratet.no/quality/completeness> .

                <https://testdirektoratet.no/quality/relevance>
                    a dqv:QualityAnnotation ;
                    dqv:inDimension <http://iso.org/25012/2008/dataquality/Relevance> ;
                    oa:hasBody [
                        rdf:value "Highly relevant" ;
                        dct:language <http://publications.europa.eu/resource/authority/language/ENG>
                    ] , [
                        rdf:value "Svært relevant" ;
                        dct:language <http://publications.europa.eu/resource/authority/language/NOB>
                    ] .

                <https://data.norge.no/vocabulary/quality-dimension#currentness>
                    skos:prefLabel  "currentness"@en, "aktualitet"@nb, "aktualitet"@nn .

                <https://testdirektoratet.no/quality/currentness>
                    a dqv:QualityAnnotation ;
                    dqv:inDimension <https://data.norge.no/vocabulary/quality-dimension#currentness> ;
                    oa:hasBody [
                        rdf:value "Updated daily"@en ;
                        dct:language <http://publications.europa.eu/resource/authority/language/ENG>
                    ] .

                <https://data.norge.no/vocabulary/quality-dimension#completeness>
                    skos:prefLabel "completeness"@en, "fullstendighet"@nb, "fullstendigheit"@nn .

                <https://testdirektoratet.no/quality/completeness>
                    a dqv:QualityAnnotation ;
                    dqv:inDimension <https://data.norge.no/vocabulary/quality-dimension#completeness> ;
                    oa:hasBody [
                        rdf:value "Complete coverage"@en ;
                        dct:language <http://publications.europa.eu/resource/authority/language/ENG>
                    ] .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

            val result = subject.extractListOfQualityAnnotations()

            assertEquals(3, result!!.size)

            val expectedRelevance =
                QualityAnnotation().apply {
                    hasBody =
                        LocalizedStrings().apply {
                            en = "Highly relevant"
                            nb = "Svært relevant"
                        }
                    inDimension = "http://iso.org/25012/2008/dataquality/Relevance"
                    qualityDimensions =
                        listOf(
                            ReferenceDataCode().apply {
                                uri = "http://iso.org/25012/2008/dataquality/Relevance"
                                code = "http://iso.org/25012/2008/dataquality/Relevance"
                            },
                        )
                }

            val expectedCurrentness =
                QualityAnnotation().apply {
                    hasBody = LocalizedStrings().apply { en = "Updated daily" }
                    inDimension = "https://data.norge.no/vocabulary/quality-dimension#currentness"
                    qualityDimensions =
                        listOf(
                            ReferenceDataCode().apply {
                                uri = "https://data.norge.no/vocabulary/quality-dimension#currentness"
                                code = "currentness"
                                prefLabel =
                                    LocalizedStrings().apply {
                                        en = "currentness"
                                        nb = "aktualitet"
                                        nn = "aktualitet"
                                    }
                            },
                        )
                }

            val expectedCompleteness =
                QualityAnnotation().apply {
                    hasBody = LocalizedStrings().apply { en = "Complete coverage" }
                    inDimension = "https://data.norge.no/vocabulary/quality-dimension#completeness"
                    qualityDimensions =
                        listOf(
                            ReferenceDataCode().apply {
                                uri = "https://data.norge.no/vocabulary/quality-dimension#completeness"
                                code = "completeness"
                                prefLabel =
                                    LocalizedStrings().apply {
                                        en = "completeness"
                                        nb = "fullstendighet"
                                        nn = "fullstendigheit"
                                    }
                            },
                        )
                }

            assertTrue(result.contains(expectedRelevance))
            assertTrue(result.contains(expectedCurrentness))
            assertTrue(result.contains(expectedCompleteness))
        }

        @Test
        fun extractReturnsNullWhenNoAnnotations() {
            val turtle =
                """
                @prefix dcat:  <http://www.w3.org/ns/dcat#> .
                @prefix dct:   <http://purl.org/dc/terms/> .

                <https://testdirektoratet.no/model/dataset/0>
                    a dcat:Dataset ;
                    dct:title "Dataset without quality annotations"@en .
                """.trimIndent()

            val m = ModelFactory.createDefaultModel()
            m.read(StringReader(turtle), null, "TURTLE")
            val subject = m.listSubjectsWithProperty(RDF.type, DCAT.Dataset).toList().first()

            val result = subject.extractListOfQualityAnnotations()

            assertNull(result)
        }
    }
}
