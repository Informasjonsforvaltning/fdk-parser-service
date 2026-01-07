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
            .setId(events.firstNonNull { it.id })
            .setUri(events.firstNonNull { it.uri })
            .setIdentifier(events.firstNonNull { it.identifier })
            .setHarvest(events.firstNonNull { it.harvest })
            .setTitle(events.firstNonNull { it.title })
            .setDescription(events.firstNonNull { it.description })
            .setDctType(events.firstNonNull { it.dctType })
            .setRelation(events.firstNonNull { it.relation })
            .setMayInitiate(events.firstNonNull { it.mayInitiate })
            .setSubject(events.firstNonNull { it.subject })
            .setDistribution(events.firstNonNull { it.distribution })
            .setCatalog(events.firstNonNull { it.catalog })
            .setSpecializedType(events.firstNonNull { it.specializedType })
            .build()
    }

    /**
     * Convenience overload for merging a prioritized event with fallback events.
     */
    fun merge(
        prioritized: Event,
        vararg fallbacks: Event,
    ): Event = merge(listOf(prioritized) + fallbacks)

    /**
     * Finds the first non-null value for a field across the prioritized events.
     */
    private inline fun <T> List<Event>.firstNonNull(extractor: (Event) -> T?): T? = this.firstNotNullOfOrNull(extractor)
}
