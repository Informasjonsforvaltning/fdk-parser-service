package no.digdir.fdk.parserservice.utils

import no.digdir.fdk.model.LocalizedStrings
import no.digdir.fdk.model.dataset.Dataset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class DatasetMergerTest {

    private fun minimal(id: String, uri: String): Dataset.Builder =
        Dataset.newBuilder()
            .setId(id)
            .setUri(uri)
            .setIdentifier(null)
            .setAdmsIdentifier(null)
            .setHarvest(null)
            .setCatalog(null)
            .setTitle(null)
            .setDescription(null)
            .setDescriptionFormatted(null)
            .setPublisher(null)
            .setDistribution(null)
            .setSample(null)
            .setContactPoint(null)
            .setThemeUris(null)
            .setTheme(null)
            .setLosTheme(null)
            .setEurovocThemes(null)
            .setKeyword(null)
            .setIssued(null)
            .setModified(null)
            .setDctType(null)
            .setAccessRights(null)
            .setLanguage(null)
            .setPage(null)
            .setLandingPage(null)
            .setTemporal(null)
            .setSubject(null)
            .setSpatial(null)
            .setProvenance(null)
            .setAccrualPeriodicity(null)
            .setLegalBasisForAccess(null)
            .setLegalBasisForProcessing(null)
            .setLegalBasisForRestriction(null)
            .setConformsTo(null)
            .setReferences(null)
            .setHasAccuracyAnnotation(null)
            .setHasAvailabilityAnnotation(null)
            .setHasCompletenessAnnotation(null)
            .setHasCurrentnessAnnotation(null)
            .setHasRelevanceAnnotation(null)
            .setQualifiedAttributions(null)
            .setIsOpenData(false)
            .setIsAuthoritative(false)
            .setIsRelatedToTransportportal(false)
            .setInSeries(null)
            .setPrev(null)
            .setLast(null)
            .setDatasetsInSeries(null)
            .setType(null)
            .setSpecializedType(null)

    @Test
    fun `should merge datasets with priority order`() {
        val highPriority = minimal("high-priority-id", "http://high").build()
        val lowPriority = minimal("low-priority-id", "http://low").build()

        val result = DatasetMerger.merge(listOf(highPriority, lowPriority))
        assertEquals("high-priority-id", result.id)
    }

    @Test
    fun `should merge datasets with vararg syntax`() {
        val prioritized = minimal("prioritized-id", "http://p").build()
        val fallback1 = minimal("fallback1-id", "http://f1").build()
        val fallback2 = minimal("fallback2-id", "http://f2").build()

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
        val dataset = minimal("single-id", "http://single").build()
        val result = DatasetMerger.merge(listOf(dataset))
        assertEquals("single-id", result.id)
    }

    @Test
    fun `should handle multiple datasets with simple merging`() {
        val dataset1 = minimal("dataset1-id", "http://1").build()
        val dataset2 = minimal("dataset2-id", "http://2").build()
        val dataset3 = minimal("dataset3-id", "http://3").build()

        val result = DatasetMerger.merge(listOf(dataset1, dataset2, dataset3))
        assertEquals("dataset1-id", result.id)
    }
}
