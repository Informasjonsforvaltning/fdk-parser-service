package no.digdir.fdk.parserservice.utils

import no.digdir.fdk.model.service.Service

/**
 * Utility object for merging multiple Service instances in priority order.
 *
 * The first service in the list is treated as highest priority; for each field the
 * first non-null/non-empty value across the list is chosen. This mirrors the merging
 * approach used for datasets.
 */
object ServiceMerger {
    /**
     * Merges a prioritized list of services into a single consolidated Service.
     *
     * @param services services ordered from highest to lowest priority
     * @return merged Service containing the first non-null values per field
     * @throws IllegalArgumentException if the input list is empty
     */
    fun merge(services: List<Service>): Service {
        require(services.isNotEmpty()) { "At least one service must be provided for merging" }

        return Service
            .newBuilder()
            .setId(services.firstNonNull { it.id })
            .setUri(services.firstNonNull { it.uri })
            .setIdentifier(services.firstNonNull { it.identifier })
            .setTitle(services.firstNonNull { it.title })
            .setDescription(services.firstNonNull { it.description })
            .setHarvest(services.firstNonNull { it.harvest })
            .setCatalog(services.firstNonNull { it.catalog })
            .setOwnedBy(services.firstNonNull { it.ownedBy })
            .setContactPoint(services.firstNonNull { it.contactPoint })
            .setKeyword(services.firstNonNull { it.keyword })
            .setSector(services.firstNonNull { it.sector })
            .setProduces(services.firstNonNull { it.produces })
            .setSpatial(services.firstNonNull { it.spatial })
            .setHasInput(services.firstNonNull { it.hasInput })
            .setProcessingTime(services.firstNonNull { it.processingTime })
            .setIsDescribedAt(services.firstNonNull { it.isDescribedAt })
            .setHasParticipation(services.firstNonNull { it.hasParticipation })
            .setIsGroupedBy(services.firstNonNull { it.isGroupedBy })
            .setIsClassifiedBy(services.firstNonNull { it.isClassifiedBy })
            .setHasChannel(services.firstNonNull { it.hasChannel })
            .setFollows(services.firstNonNull { it.follows })
            .setHasCost(services.firstNonNull { it.hasCost })
            .setRequires(services.firstNonNull { it.requires })
            .setRelation(services.firstNonNull { it.relation })
            .setHasLegalResource(services.firstNonNull { it.hasLegalResource })
            .setLanguage(services.firstNonNull { it.language })
            .setHoldsRequirement(services.firstNonNull { it.holdsRequirement })
            .setAdmsStatus(services.firstNonNull { it.admsStatus })
            .setSubject(services.firstNonNull { it.subject })
            .setHomepage(services.firstNonNull { it.homepage })
            .setDctType(services.firstNonNull { it.dctType })
            .setThematicAreaUris(services.firstNonNull { it.thematicAreaUris })
            .setLosThemes(services.firstNonNull { it.losThemes })
            .setEurovocThemes(services.firstNonNull { it.eurovocThemes })
            .setParticipatingAgents(services.firstNonNull { it.participatingAgents })
            .setHasCompetentAuthority(services.firstNonNull { it.hasCompetentAuthority })
            .setType(services.firstNonNull { it.type })
            .setSpecializedType(services.firstNonNull { it.specializedType })
            .build()
    }

    /**
     * Convenience overload for merging a prioritized service with fallback services.
     */
    fun merge(
        prioritized: Service,
        vararg fallbacks: Service,
    ): Service = merge(listOf(prioritized) + fallbacks)

    /**
     * Finds the first non-null value for a field across the prioritized services.
     */
    private inline fun <T> List<Service>.firstNonNull(extractor: (Service) -> T?): T? = this.firstNotNullOfOrNull(extractor)
}
