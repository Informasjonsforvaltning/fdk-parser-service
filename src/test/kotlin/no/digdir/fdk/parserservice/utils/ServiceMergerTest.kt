package no.digdir.fdk.parserservice.utils

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.ReferenceDataCode
import no.digdir.fdk.model.service.Service
import no.digdir.fdk.model.service.ServiceChannel
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@Tag("unit")
class ServiceMergerTest {
    private fun minimal(
        id: String,
        uri: String,
    ): Service =
        Service().apply {
            this.id = id
            this.uri = uri
        }

    @Test
    fun `should merge services with priority order`() {
        val high =
            minimal("high-id", "http://high").apply {
                title = LocalizedStrings().apply { en = "High title" }
            }
        val low =
            minimal("low-id", "http://low").apply {
                description = LocalizedStrings().apply { en = "Only low description" }
                language = listOf(ReferenceDataCode().apply { uri = "lang-low" })
            }

        val merged = ServiceMerger.merge(listOf(high, low))

        assertEquals("high-id", merged.id)
        assertEquals("http://high", merged.uri)
        assertEquals("High title", merged.title.en)
        assertEquals("Only low description", merged.description.en)
        assertEquals("lang-low", merged.language.first().uri)
    }

    @Test
    fun `should merge services with vararg syntax`() {
        val prioritized = minimal("p-id", "http://p")
        val fallback1 = minimal("f1-id", "http://f1")
        val fallback2 = minimal("f2-id", "http://f2")

        val merged = ServiceMerger.merge(prioritized, fallback1, fallback2)
        assertEquals("p-id", merged.id)
        assertEquals("http://p", merged.uri)
    }

    @Test
    fun `should throw exception for empty service list`() {
        assertThrows<IllegalArgumentException> {
            ServiceMerger.merge(emptyList())
        }
    }

    @Test
    fun `should handle single service`() {
        val service = minimal("single-id", "http://single")
        val merged = ServiceMerger.merge(listOf(service))
        assertEquals("single-id", merged.id)
        assertEquals("http://single", merged.uri)
    }

    @Test
    fun `should take first non-null values across multiple services`() {
        val s1 =
            minimal("id1", "http://1").apply {
                keyword = listOf(LocalizedStrings().apply { en = "kw1" })
                hasCost = null
            }
        val s2 =
            minimal("id2", "http://2").apply {
                keyword = listOf(LocalizedStrings().apply { en = "kw2" })
                hasCost = emptyList()
                hasChannel = null
            }
        val s3 =
            minimal("id3", "http://3").apply {
                keyword = null
                hasCost = null
                hasChannel = listOf(ServiceChannel().apply { identifier = "channel-1" })
            }

        val merged = ServiceMerger.merge(listOf(s1, s2, s3))

        assertEquals("id1", merged.id)
        assertEquals(listOf(ServiceChannel().apply { identifier = "channel-1" }), merged.hasChannel)
        assertEquals("kw1", merged.keyword.first().en)
        assertEquals(emptyList(), merged.hasCost)
    }
}
