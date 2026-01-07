package no.digdir.fdk.parserservice.utils

import no.digdir.fdk.model.HarvestMetaData
import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.event.Event
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@Tag("unit")
class EventMergerTest {
    private fun minimal(
        id: String,
        uri: String,
    ): Event =
        Event().apply {
            this.id = id
            this.uri = uri
        }

    @Test
    fun `should merge events with priority order`() {
        val high =
            minimal("high-id", "http://high").apply {
                title = LocalizedStrings().apply { en = "High title" }
            }
        val low =
            minimal("low-id", "http://low").apply {
                description = LocalizedStrings().apply { en = "Only low description" }
                relation = listOf("relation-low")
            }

        val merged = EventMerger.merge(listOf(high, low))

        assertEquals("high-id", merged.id)
        assertEquals("http://high", merged.uri)
        assertEquals("High title", merged.title?.en)
        assertEquals("Only low description", merged.description?.en)
        assertEquals("relation-low", merged.relation?.first())
    }

    @Test
    fun `should merge events with vararg syntax`() {
        val prioritized = minimal("p-id", "http://p")
        val fallback1 = minimal("f1-id", "http://f1")
        val fallback2 = minimal("f2-id", "http://f2")

        val merged = EventMerger.merge(prioritized, fallback1, fallback2)
        assertEquals("p-id", merged.id)
        assertEquals("http://p", merged.uri)
    }

    @Test
    fun `should throw exception for empty event list`() {
        assertThrows<IllegalArgumentException> {
            EventMerger.merge(emptyList())
        }
    }

    @Test
    fun `should handle single event`() {
        val event = minimal("single-id", "http://single")
        val merged = EventMerger.merge(listOf(event))
        assertEquals("single-id", merged.id)
        assertEquals("http://single", merged.uri)
    }

    @Test
    fun `should take first non-null values across multiple events`() {
        val e1 =
            minimal("id1", "http://1").apply {
                identifier = "identifier-1"
                mayInitiate = listOf("initiate-1")
                subject = null
            }
        val e2 =
            minimal("id2", "http://2").apply {
                identifier = "identifier-2"
                mayInitiate = null
                subject = listOf("subject-2")
                distribution = emptyList()
            }
        val e3 =
            minimal("id3", "http://3").apply {
                identifier = null
                mayInitiate = null
                subject = null
                distribution = listOf("distribution-3")
            }

        val merged = EventMerger.merge(listOf(e1, e2, e3))

        assertEquals("id1", merged.id)
        assertEquals("identifier-1", merged.identifier)
        assertEquals(listOf("initiate-1"), merged.mayInitiate)
        assertEquals(listOf("subject-2"), merged.subject)
        assertEquals(emptyList(), merged.distribution)
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

        val merged = EventMerger.merge(listOf(high, low))

        assertEquals("2020-01-01T00:00:00Z", merged.harvest?.firstHarvested)
        assertEquals("2020-01-02T00:00:00Z", merged.harvest?.modified)
    }

    @Test
    fun `should merge dctType with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                dctType =
                    listOf(
                        ReferenceDataCode().apply {
                            uri = "type-high"
                            prefLabel = LocalizedStrings().apply { en = "High Type" }
                        },
                    )
            }
        val low =
            minimal("low-id", "http://low").apply {
                dctType =
                    listOf(
                        ReferenceDataCode().apply {
                            uri = "type-low"
                            prefLabel = LocalizedStrings().apply { en = "Low Type" }
                        },
                    )
            }

        val merged = EventMerger.merge(listOf(high, low))

        assertEquals("type-high", merged.dctType?.first()?.uri)
        assertEquals(
            "High Type",
            merged.dctType
                ?.first()
                ?.prefLabel
                ?.en,
        )
    }

    @Test
    fun `should merge specializedType with priority`() {
        val high =
            minimal("high-id", "http://high").apply {
                specializedType = "business_event"
            }
        val low =
            minimal("low-id", "http://low").apply {
                specializedType = "life_event"
            }

        val merged = EventMerger.merge(listOf(high, low))

        assertEquals("business_event", merged.specializedType)
    }
}
