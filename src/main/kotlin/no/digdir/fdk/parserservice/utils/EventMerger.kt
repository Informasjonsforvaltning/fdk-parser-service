package no.digdir.fdk.parserservice.utils

import no.digdir.fdk.model.event.Event

/**
 * Utility object for merging multiple Event instances in priority order.
 *
 * The first event in the list is treated as highest priority; for each field the
 * first non-null/non-empty value across the list is chosen. This mirrors the merging
 * approach used for datasets and services.
 */
object EventMerger {
    /**
     * Merges a prioritized list of events into a single consolidated Event.
     *
     * @param events events ordered from highest to lowest priority
     * @return merged Event containing the first non-null values per field
     * @throws IllegalArgumentException if the input list is empty
     */
    fun merge(events: List<Event>): Event {
        require(events.isNotEmpty()) { "At least one event must be provided for merging" }

        return Event
            .newBuilder()
            .setId(events.firstNotNullOfOrNull { it.id })
            .setUri(events.firstNotNullOfOrNull { it.uri })
            .setIdentifier(events.firstNotNullOfOrNull { it.identifier })
            .setHarvest(events.firstNotNullOfOrNull { it.harvest })
            .setTitle(events.firstNotNullOfOrNull { it.title })
            .setDescription(events.firstNotNullOfOrNull { it.description })
            .setDctType(events.firstNotNullOfOrNull { it.dctType })
            .setRelation(events.firstNotNullOfOrNull { it.relation })
            .setMayInitiate(events.firstNotNullOfOrNull { it.mayInitiate })
            .setSubject(events.firstNotNullOfOrNull { it.subject })
            .setDistribution(events.firstNotNullOfOrNull { it.distribution })
            .setCatalog(events.firstNotNullOfOrNull { it.catalog })
            .setSpecializedType(events.firstNotNullOfOrNull { it.specializedType })
            .build()
    }

    /**
     * Convenience overload for merging a prioritized event with fallback events.
     */
    fun merge(
        prioritized: Event,
        vararg fallbacks: Event,
    ): Event = merge(listOf(prioritized) + fallbacks)
}
