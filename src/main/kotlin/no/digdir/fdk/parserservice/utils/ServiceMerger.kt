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
            .setId(services.firstNotNullOfOrNull { it.id })
            .setUri(services.firstNotNullOfOrNull { it.uri })
            .setIdentifier(services.firstNotNullOfOrNull { it.identifier })
            .setTitle(services.firstNotNullOfOrNull { it.title })
            .setDescription(services.firstNotNullOfOrNull { it.description })
            .setHarvest(services.firstNotNullOfOrNull { it.harvest })
            .setCatalog(services.firstNotNullOfOrNull { it.catalog })
            .setOwnedBy(services.firstNotNullOfOrNull { it.ownedBy })
            .setContactPoint(services.firstNotNullOfOrNull { it.contactPoint })
            .setKeyword(services.firstNotNullOfOrNull { it.keyword })
            .setSector(services.firstNotNullOfOrNull { it.sector })
            .setProduces(services.firstNotNullOfOrNull { it.produces })
            .setSpatial(services.firstNotNullOfOrNull { it.spatial })
            .setHasInput(services.firstNotNullOfOrNull { it.hasInput })
            .setProcessingTime(services.firstNotNullOfOrNull { it.processingTime })
            .setIsDescribedAt(services.firstNotNullOfOrNull { it.isDescribedAt })
            .setHasParticipation(services.firstNotNullOfOrNull { it.hasParticipation })
            .setIsGroupedBy(services.firstNotNullOfOrNull { it.isGroupedBy })
            .setIsClassifiedBy(services.firstNotNullOfOrNull { it.isClassifiedBy })
            .setHasChannel(services.firstNotNullOfOrNull { it.hasChannel })
            .setFollows(services.firstNotNullOfOrNull { it.follows })
            .setCosts(services.firstNotNullOfOrNull { it.costs })
            .setRequires(services.firstNotNullOfOrNull { it.requires })
            .setRelation(services.firstNotNullOfOrNull { it.relation })
            .setHasLegalResource(services.firstNotNullOfOrNull { it.hasLegalResource })
            .setLanguage(services.firstNotNullOfOrNull { it.language })
            .setHoldsRequirement(services.firstNotNullOfOrNull { it.holdsRequirement })
            .setAdmsStatus(services.firstNotNullOfOrNull { it.admsStatus })
            .setSubject(services.firstNotNullOfOrNull { it.subject })
            .setHomepage(services.firstNotNullOfOrNull { it.homepage })
            .setDctType(services.firstNotNullOfOrNull { it.dctType })
            .setThematicAreaUris(services.firstNotNullOfOrNull { it.thematicAreaUris })
            .setLosThemes(services.firstNotNullOfOrNull { it.losThemes })
            .setEurovocThemes(services.firstNotNullOfOrNull { it.eurovocThemes })
            .setParticipatingAgents(services.firstNotNullOfOrNull { it.participatingAgents })
            .setHasCompetentAuthority(services.firstNotNullOfOrNull { it.hasCompetentAuthority })
            .setType(services.firstNotNullOfOrNull { it.type })
            .setSpecializedType(services.firstNotNullOfOrNull { it.specializedType })
            .build()
    }

    /**
     * Convenience overload for merging a prioritized service with fallback services.
     */
    fun merge(prioritized: Service, vararg fallbacks: Service): Service = merge(listOf(prioritized) + fallbacks)
}
