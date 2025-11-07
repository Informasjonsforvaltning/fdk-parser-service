package no.digdir.fdk.parserservice.utils

import no.digdir.fdk.model.dataset.Dataset

/**
 * Utility class for merging multiple Dataset objects in a prioritized manner.
 *
 * This class provides an efficient approach to combining datasets by prioritizing
 * non-null values from higher priority datasets over lower priority ones.
 * It uses direct method calls for optimal performance and type safety.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
object DatasetMerger {
    /**
     * Merges multiple datasets in priority order (first dataset has highest priority).
     *
     * @param datasets List of datasets in priority order (highest to lowest priority)
     * @return A new Dataset with values from the highest priority non-null source
     * @throws IllegalArgumentException if datasets list is empty
     */
    fun merge(datasets: List<Dataset>): Dataset {
        require(datasets.isNotEmpty()) { "At least one dataset must be provided for merging" }

        return Dataset
            .newBuilder()
            .setId(datasets.firstNonNull { it.id })
            .setUri(datasets.firstNonNull { it.uri })
            .setIdentifier(datasets.firstNonNull { it.identifier })
            .setAdmsIdentifier(datasets.firstNonNull { it.admsIdentifier })
            .setHarvest(datasets.firstNonNull { it.harvest })
            .setCatalog(datasets.firstNonNull { it.catalog })
            .setTitle(datasets.firstNonNull { it.title })
            .setDescription(datasets.firstNonNull { it.description })
            .setDescriptionFormatted(datasets.firstNonNull { it.descriptionFormatted })
            .setPublisher(datasets.firstNonNull { it.publisher })
            .setDistribution(datasets.firstNonNull { it.distribution })
            .setSample(datasets.firstNonNull { it.sample })
            .setContactPoint(datasets.firstNonNull { it.contactPoint })
            .setThemeUris(datasets.firstNonNull { it.themeUris })
            .setTheme(datasets.firstNonNull { it.theme })
            .setLosTheme(datasets.firstNonNull { it.losTheme })
            .setEurovocThemes(datasets.firstNonNull { it.eurovocThemes })
            .setMobilityTheme(datasets.firstNonNull { it.mobilityTheme })
            .setKeyword(datasets.firstNonNull { it.keyword })
            .setIssued(datasets.firstNonNull { it.issued })
            .setModified(datasets.firstNonNull { it.modified })
            .setDctType(datasets.firstNonNull { it.dctType })
            .setAccessRights(datasets.firstNonNull { it.accessRights })
            .setLanguage(datasets.firstNonNull { it.language })
            .setPage(datasets.firstNonNull { it.page })
            .setLandingPage(datasets.firstNonNull { it.landingPage })
            .setTemporal(datasets.firstNonNull { it.temporal })
            .setSubject(datasets.firstNonNull { it.subject })
            .setSpatial(datasets.firstNonNull { it.spatial })
            .setProvenance(datasets.firstNonNull { it.provenance })
            .setAccrualPeriodicity(datasets.firstNonNull { it.accrualPeriodicity })
            .setLegalBasisForAccess(datasets.firstNonNull { it.legalBasisForAccess })
            .setLegalBasisForProcessing(datasets.firstNonNull { it.legalBasisForProcessing })
            .setLegalBasisForRestriction(datasets.firstNonNull { it.legalBasisForRestriction })
            .setConformsTo(datasets.firstNonNull { it.conformsTo })
            .setReferences(datasets.firstNonNull { it.references })
            .setHasAccuracyAnnotation(datasets.firstNonNull { it.hasAccuracyAnnotation })
            .setHasAvailabilityAnnotation(datasets.firstNonNull { it.hasAvailabilityAnnotation })
            .setHasCompletenessAnnotation(datasets.firstNonNull { it.hasCompletenessAnnotation })
            .setHasCurrentnessAnnotation(datasets.firstNonNull { it.hasCurrentnessAnnotation })
            .setHasRelevanceAnnotation(datasets.firstNonNull { it.hasRelevanceAnnotation })
            .setQualifiedAttributions(datasets.firstNonNull { it.qualifiedAttributions })
            .setIsAuthoritative(datasets.firstNonNull { it.isAuthoritative } ?: false)
            .setIsOpenData(datasets.firstNonNull { it.isOpenData } ?: false)
            .setIsRelatedToTransportportal(datasets.firstNonNull { it.isRelatedToTransportportal } ?: false)
            .setInSeries(datasets.firstNonNull { it.inSeries })
            .setPrev(datasets.firstNonNull { it.prev })
            .setLast(datasets.firstNonNull { it.last })
            .setDatasetsInSeries(datasets.firstNonNull { it.datasetsInSeries })
            .setType(datasets.firstNonNull { it.type })
            .setSpecializedType(datasets.firstNonNull { it.specializedType })
            .build()
    }

    /**
     * Merges datasets with explicit priority order.
     *
     * @param prioritized The highest priority dataset
     * @param fallbacks Additional datasets in priority order
     * @return A new Dataset with values from the highest priority non-null source
     */
    fun merge(
        prioritized: Dataset,
        vararg fallbacks: Dataset,
    ): Dataset = merge(listOf(prioritized) + fallbacks)

    /**
     * Helper function to find the first non-null value from prioritized datasets.
     * This is a more efficient alternative to reflection-based property access.
     */
    private inline fun <T> List<Dataset>.firstNonNull(extractor: (Dataset) -> T?): T? = this.firstNotNullOfOrNull(extractor)
}
