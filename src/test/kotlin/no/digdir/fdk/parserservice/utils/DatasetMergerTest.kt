package no.digdir.fdk.parserservice.utils

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.dataset.Dataset
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@Tag("unit")
class DatasetMergerTest {
    private fun minimal(
        id: String,
        uri: String,
    ): Dataset =
        Dataset().apply {
            this.id = id
            this.uri = uri
        }

    @Test
    fun `should merge datasets with priority order`() {
        val highPriority = minimal("high-priority-id", "http://high")
        val lowPriority =
            minimal("low-priority-id", "http://low")
                .apply { title = LocalizedStrings().apply { en = "Title only available in low" } }

        val result = DatasetMerger.merge(listOf(highPriority, lowPriority))
        assertEquals("high-priority-id", result.id)
        assertEquals("http://high", result.uri)
        assertEquals("Title only available in low", result.title.en)
    }

    @Test
    fun `should merge datasets with vararg syntax`() {
        val prioritized = minimal("prioritized-id", "http://p")
        val fallback1 = minimal("fallback1-id", "http://f1")
        val fallback2 = minimal("fallback2-id", "http://f2")

        val result = DatasetMerger.merge(prioritized, fallback1, fallback2)
        assertEquals("prioritized-id", result.id)
    }

    @Test
    fun `should throw exception for empty dataset list`() {
        assertThrows<IllegalArgumentException> {
            DatasetMerger.merge(emptyList())
        }
    }

    @Test
    fun `should handle single dataset`() {
        val dataset = minimal("single-id", "http://single")
        val result = DatasetMerger.merge(listOf(dataset))
        assertEquals("single-id", result.id)
    }

    @Test
    fun `should handle multiple datasets with simple merging`() {
        val dataset1 = minimal("dataset1-id", "http://1")
        val dataset2 = minimal("dataset2-id", "http://2")
        val dataset3 = minimal("dataset3-id", "http://3")

        val result = DatasetMerger.merge(listOf(dataset1, dataset2, dataset3))
        assertEquals("dataset1-id", result.id)
    }
}
