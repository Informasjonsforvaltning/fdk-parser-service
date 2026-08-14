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
            .setId(datasets.firstNotNullOfOrNull { it.id })
            .setUri(datasets.firstNotNullOfOrNull { it.uri })
            .setIdentifier(datasets.firstNotNullOfOrNull { it.identifier })
            .setAdmsIdentifier(datasets.firstNotNullOfOrNull { it.admsIdentifier })
            .setHarvest(datasets.firstNotNullOfOrNull { it.harvest })
            .setCatalog(datasets.firstNotNullOfOrNull { it.catalog })
            .setTitle(datasets.firstNotNullOfOrNull { it.title })
            .setDescription(datasets.firstNotNullOfOrNull { it.description })
            .setDescriptionFormatted(datasets.firstNotNullOfOrNull { it.descriptionFormatted })
            .setPublisher(datasets.firstNotNullOfOrNull { it.publisher })
            .setDistribution(datasets.firstNotNullOfOrNull { it.distribution })
            .setSample(datasets.firstNotNullOfOrNull { it.sample })
            .setContactPoint(datasets.firstNotNullOfOrNull { it.contactPoint })
            .setHvdCategory(datasets.firstNotNullOfOrNull { it.hvdCategory })
            .setThemeUris(datasets.firstNotNullOfOrNull { it.themeUris })
            .setTheme(datasets.firstNotNullOfOrNull { it.theme })
            .setLosTheme(datasets.firstNotNullOfOrNull { it.losTheme })
            .setEurovocThemes(datasets.firstNotNullOfOrNull { it.eurovocThemes })
            .setMobilityTheme(datasets.firstNotNullOfOrNull { it.mobilityTheme })
            .setKeyword(datasets.firstNotNullOfOrNull { it.keyword })
            .setIssued(datasets.firstNotNullOfOrNull { it.issued })
            .setModified(datasets.firstNotNullOfOrNull { it.modified })
            .setDctType(datasets.firstNotNullOfOrNull { it.dctType })
            .setAccessRights(datasets.firstNotNullOfOrNull { it.accessRights })
            .setLanguage(datasets.firstNotNullOfOrNull { it.language })
            .setPage(datasets.firstNotNullOfOrNull { it.page })
            .setLandingPage(datasets.firstNotNullOfOrNull { it.landingPage })
            .setTemporal(datasets.firstNotNullOfOrNull { it.temporal })
            .setSubject(datasets.firstNotNullOfOrNull { it.subject })
            .setSpatial(datasets.firstNotNullOfOrNull { it.spatial })
            .setProvenance(datasets.firstNotNullOfOrNull { it.provenance })
            .setAccrualPeriodicity(datasets.firstNotNullOfOrNull { it.accrualPeriodicity })
            .setLegalBasisForAccess(datasets.firstNotNullOfOrNull { it.legalBasisForAccess })
            .setLegalBasisForProcessing(datasets.firstNotNullOfOrNull { it.legalBasisForProcessing })
            .setLegalBasisForRestriction(datasets.firstNotNullOfOrNull { it.legalBasisForRestriction })
            .setApplicableLegislation(datasets.firstNotNullOfOrNull { it.applicableLegislation })
            .setConformsTo(datasets.firstNotNullOfOrNull { it.conformsTo })
            .setReferences(datasets.firstNotNullOfOrNull { it.references })
            .setHasAccuracyAnnotation(datasets.firstNotNullOfOrNull { it.hasAccuracyAnnotation })
            .setHasAvailabilityAnnotation(datasets.firstNotNullOfOrNull { it.hasAvailabilityAnnotation })
            .setHasCompletenessAnnotation(datasets.firstNotNullOfOrNull { it.hasCompletenessAnnotation })
            .setHasCurrentnessAnnotation(datasets.firstNotNullOfOrNull { it.hasCurrentnessAnnotation })
            .setHasRelevanceAnnotation(datasets.firstNotNullOfOrNull { it.hasRelevanceAnnotation })
            .setQualityAnnotations(datasets.firstNotNullOfOrNull { it.qualityAnnotations })
            .setQualifiedAttributions(datasets.firstNotNullOfOrNull { it.qualifiedAttributions })
            .setCosts(datasets.firstNotNullOfOrNull { it.costs })
            .setIsAuthoritative(datasets.firstNotNullOfOrNull { it.isAuthoritative } ?: false)
            .setIsOpenData(datasets.firstNotNullOfOrNull { it.isOpenData } ?: false)
            .setIsRelatedToTransportportal(datasets.firstNotNullOfOrNull { it.isRelatedToTransportportal } ?: false)
            .setInSeries(datasets.firstNotNullOfOrNull { it.inSeries })
            .setPrev(datasets.firstNotNullOfOrNull { it.prev })
            .setLast(datasets.firstNotNullOfOrNull { it.last })
            .setDatasetsInSeries(datasets.firstNotNullOfOrNull { it.datasetsInSeries })
            .setType(datasets.firstNotNullOfOrNull { it.type })
            .setSpecializedType(datasets.firstNotNullOfOrNull { it.specializedType })
            .build()
    }

    /**
     * Merges datasets with explicit priority order.
     *
     * @param prioritized The highest priority dataset
     * @param fallbacks Additional datasets in priority order
     * @return A new Dataset with values from the highest priority non-null source
     */
    fun merge(prioritized: Dataset, vararg fallbacks: Dataset): Dataset = merge(listOf(prioritized) + fallbacks)
}
