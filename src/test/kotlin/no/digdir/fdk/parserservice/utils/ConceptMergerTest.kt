package no.digdir.fdk.parserservice.utils

import no.digdir.fdk.model.ContactPoint
import no.digdir.fdk.model.HarvestMetaData
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ResourceType
import no.digdir.fdk.model.concept.Concept
import no.digdir.fdk.model.concept.ConceptAssociativeRelation
import no.digdir.fdk.model.concept.ConceptCollection
import no.digdir.fdk.model.concept.ConceptDefinition
import no.digdir.fdk.model.concept.ConceptGenericRelation
import no.digdir.fdk.model.concept.ConceptPartitiveRelation
import no.digdir.fdk.model.concept.ConceptSubject
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@Tag("unit")
class ConceptMergerTest {
    private fun minimal(
        id: String,
        uri: String,
    ): Concept =
        Concept().apply {
            this.id = id
            this.uri = uri
        }

    @Test
    fun `should merge concepts with priority order`() {
        val high =
            minimal("id", "http://high").apply {
                prefLabel = LocalizedStrings().apply { en = "High label" }
            }
        val low =
            minimal("id", "http://low").apply {
                example = LocalizedStrings().apply { en = "Only low example" }
                seeAlso = listOf("http://see-also-low")
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("id", merged.id)
        assertEquals("http://high", merged.uri)
        assertEquals("High label", merged.prefLabel?.en)
        assertEquals("Only low example", merged.example?.en)
        assertEquals("http://see-also-low", merged.seeAlso?.first())
    }

    @Test
    fun `should merge concepts with vararg syntax`() {
        val prioritized = minimal("p-id", "http://p")
        val fallback1 = minimal("f1-id", "http://f1")
        val fallback2 = minimal("f2-id", "http://f2")

        val merged = ConceptMerger.merge(prioritized, fallback1, fallback2)
        assertEquals("p-id", merged.id)
        assertEquals("http://p", merged.uri)
    }

    @Test
    fun `should throw exception for empty concept list`() {
        assertThrows<IllegalArgumentException> {
            ConceptMerger.merge(emptyList())
        }
    }

    @Test
    fun `should handle single concept`() {
        val concept = minimal("single-id", "http://single")
        val merged = ConceptMerger.merge(listOf(concept))
        assertEquals("single-id", merged.id)
        assertEquals("http://single", merged.uri)
    }

    @Test
    fun `should take first non-null values across multiple concepts`() {
        val c1 =
            minimal("id1", "http://1").apply {
                identifier = "identifier-1"
                prefLabel = LocalizedStrings().apply { en = "Label 1" }
                altLabel = null
            }
        val c2 =
            minimal("id2", "http://2").apply {
                identifier = "identifier-2"
                prefLabel = null
                altLabel = listOf(LocalizedStrings().apply { en = "Alt label 2" })
                hiddenLabel = null
            }
        val c3 =
            minimal("id3", "http://3").apply {
                identifier = null
                prefLabel = null
                altLabel = null
                hiddenLabel = listOf(LocalizedStrings().apply { en = "Hidden label 3" })
            }

        val merged = ConceptMerger.merge(listOf(c1, c2, c3))

        assertEquals("id1", merged.id)
        assertEquals("identifier-1", merged.identifier)
        assertEquals("Label 1", merged.prefLabel?.en)
        assertEquals("Alt label 2", merged.altLabel?.first()?.en)
        assertEquals("Hidden label 3", merged.hiddenLabel?.first()?.en)
    }

    @Test
    fun `should merge harvest metadata with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                harvest =
                    HarvestMetaData().apply {
                        firstHarvested = "2020-01-01T00:00:00Z"
                        modified = "2020-01-02T00:00:00Z"
                    }
            }
        val low =
            minimal("low-id", "http://low").apply {
                harvest =
                    HarvestMetaData().apply {
                        firstHarvested = "2021-01-01T00:00:00Z"
                        modified = "2021-01-02T00:00:00Z"
                    }
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("2020-01-01T00:00:00Z", merged.harvest?.firstHarvested)
        assertEquals("2020-01-02T00:00:00Z", merged.harvest?.modified)
    }

    @Test
    fun `should merge definitions with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                definition =
                    ConceptDefinition().apply {
                        text = LocalizedStrings().apply { en = "High definition" }
                    }
                definitions =
                    listOf(
                        ConceptDefinition().apply {
                            text = LocalizedStrings().apply { en = "High definition" }
                        },
                    )
            }
        val low =
            minimal("low-id", "http://low").apply {
                definition =
                    ConceptDefinition().apply {
                        text = LocalizedStrings().apply { en = "Low definition" }
                    }
                definitions =
                    listOf(
                        ConceptDefinition().apply {
                            text = LocalizedStrings().apply { en = "Low definition" }
                        },
                    )
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("High definition", merged.definition?.text?.en)
        assertEquals(
            "High definition",
            merged.definitions
                ?.first()
                ?.text
                ?.en,
        )
    }

    @Test
    fun `should merge relations with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                associativeRelation =
                    listOf(
                        ConceptAssociativeRelation().apply {
                            description = LocalizedStrings().apply { en = "High associative" }
                            related = "http://related-high"
                        },
                    )
            }
        val low =
            minimal("low-id", "http://low").apply {
                associativeRelation =
                    listOf(
                        ConceptAssociativeRelation().apply {
                            description = LocalizedStrings().apply { en = "Low associative" }
                            related = "http://related-low"
                        },
                    )
                partitiveRelation =
                    listOf(
                        ConceptPartitiveRelation().apply {
                            description = LocalizedStrings().apply { en = "Low partitive" }
                        },
                    )
                genericRelation =
                    listOf(
                        ConceptGenericRelation().apply {
                            divisioncriterion = LocalizedStrings().apply { en = "Low generic" }
                        },
                    )
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals(
            "High associative",
            merged.associativeRelation
                ?.first()
                ?.description
                ?.en,
        )
        assertEquals("http://related-high", merged.associativeRelation?.first()?.related)
        assertEquals(
            "Low partitive",
            merged.partitiveRelation
                ?.first()
                ?.description
                ?.en,
        )
        assertEquals(
            "Low generic",
            merged.genericRelation
                ?.first()
                ?.divisioncriterion
                ?.en,
        )
    }

    @Test
    fun `should merge status and type with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                status = LocalizedStrings().apply { en = "Published" }
                type = ResourceType.concept
            }
        val low =
            minimal("low-id", "http://low").apply {
                status = LocalizedStrings().apply { en = "Draft" }
                type = null
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("Published", merged.status?.en)
        assertEquals(ResourceType.concept, merged.type)
    }

    @Test
    fun `should merge collection with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                collection =
                    ConceptCollection().apply {
                        uri = "http://collection-high"
                        label = LocalizedStrings().apply { en = "High collection" }
                    }
            }
        val low =
            minimal("low-id", "http://low").apply {
                collection =
                    ConceptCollection().apply {
                        uri = "http://collection-low"
                        label = LocalizedStrings().apply { en = "Low collection" }
                    }
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("http://collection-high", merged.collection?.uri)
        assertEquals("High collection", merged.collection?.label?.en)
    }

    @Test
    fun `should merge contact point with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                contactPoint =
                    ContactPoint().apply {
                        email = "high@example.com"
                    }
            }
        val low =
            minimal("low-id", "http://low").apply {
                contactPoint =
                    ContactPoint().apply {
                        email = "low@example.com"
                    }
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("high@example.com", merged.contactPoint?.email)
    }

    @Test
    fun `should merge subjects with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                subject =
                    listOf(
                        ConceptSubject().apply {
                            uri = "http://subject-high"
                            label = LocalizedStrings().apply { en = "High subject" }
                        },
                    )
            }
        val low =
            minimal("low-id", "http://low").apply {
                subject =
                    listOf(
                        ConceptSubject().apply {
                            uri = "http://subject-low"
                            label = LocalizedStrings().apply { en = "Low subject" }
                        },
                    )
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("http://subject-high", merged.subject?.first()?.uri)
        assertEquals(
            "High subject",
            merged.subject
                ?.first()
                ?.label
                ?.en,
        )
    }

    @Test
    fun `should merge validity dates with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                validFromIncluding = "2020-01-01"
                validToIncluding = "2020-12-31"
            }
        val low =
            minimal("low-id", "http://low").apply {
                validFromIncluding = "2021-01-01"
                validToIncluding = "2021-12-31"
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("2020-01-01", merged.validFromIncluding)
        assertEquals("2020-12-31", merged.validToIncluding)
    }

    @Test
    fun `should merge skos relationships with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                exactMatch = listOf("http://exact-high")
                closeMatch = null
                memberOf = null
            }
        val low =
            minimal("low-id", "http://low").apply {
                exactMatch = listOf("http://exact-low")
                closeMatch = listOf("http://close-low")
                memberOf = listOf("http://member-low")
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("http://exact-high", merged.exactMatch?.first())
        assertEquals("http://close-low", merged.closeMatch?.first())
        assertEquals("http://member-low", merged.memberOf?.first())
    }

    @Test
    fun `should merge replaces relationships with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                isReplacedBy = listOf("http://replaced-by-high")
                replaces = null
            }
        val low =
            minimal("low-id", "http://low").apply {
                isReplacedBy = listOf("http://replaced-by-low")
                replaces = listOf("http://replaces-low")
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("http://replaced-by-high", merged.isReplacedBy?.first())
        assertEquals("http://replaces-low", merged.replaces?.first())
    }

    @Test
    fun `should merge remark and range with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                remark = LocalizedStrings().apply { en = "High remark" }
            }
        val low =
            minimal("low-id", "http://low").apply {
                remark = LocalizedStrings().apply { en = "Low remark" }
            }

        val merged = ConceptMerger.merge(listOf(high, low))

        assertEquals("High remark", merged.remark?.en)
    }

    @Test
    fun `should handle null and empty list differences`() {
        val c1 =
            minimal("id1", "http://1").apply {
                altLabel = null
            }
        val c2 =
            minimal("id2", "http://2").apply {
                altLabel = emptyList()
            }
        val c3 =
            minimal("id3", "http://3").apply {
                altLabel = listOf(LocalizedStrings().apply { en = "Alt" })
            }

        val merged = ConceptMerger.merge(listOf(c1, c2, c3))

        assertEquals(emptyList(), merged.altLabel)
    }
}
