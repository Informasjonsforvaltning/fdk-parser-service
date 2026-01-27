package no.digdir.fdk.parserservice.utils

import no.digdir.fdk.model.concept.Concept

/**
 * Utility object for merging multiple Concept instances in a prioritized manner.
 *
 * The first concept in the list is treated as highest priority; for each field the
 * first non-null/non-empty value across the list is chosen.
 * It uses direct method calls for optimal performance and type safety.
 *
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
object ConceptMerger {
    /**
     * Merges a prioritized list of concepts into a single consolidated Concept.
     *
     * @param concepts List of concepts ordered from highest to lowest priority
     * @return merged Concept containing the first non-null values per field
     * @throws IllegalArgumentException if the input list is empty
     */
    fun merge(concepts: List<Concept>): Concept {
        require(concepts.isNotEmpty()) { "At least one concept must be provided for merging" }

        return Concept
            .newBuilder()
            .setId(concepts.firstNonNull { it.id })
            .setUri(concepts.firstNonNull { it.uri })
            .setIdentifier(concepts.firstNonNull { it.identifier })
            .setHarvest(concepts.firstNonNull { it.harvest })
            .setType(concepts.firstNonNull { it.type })
            .setCollection(concepts.firstNonNull { it.collection })
            .setPublisher(concepts.firstNonNull { it.publisher })
            .setCreator(concepts.firstNonNull { it.creator })
            .setSubject(concepts.firstNonNull { it.subject })
            .setStatus(concepts.firstNonNull { it.status })
            .setExample(concepts.firstNonNull { it.example })
            .setPrefLabel(concepts.firstNonNull { it.prefLabel })
            .setHiddenLabel(concepts.firstNonNull { it.hiddenLabel })
            .setAltLabel(concepts.firstNonNull { it.altLabel })
            .setContactPoint(concepts.firstNonNull { it.contactPoint })
            .setDefinition(concepts.firstNonNull { it.definition })
            .setDefinitions(concepts.firstNonNull { it.definitions })
            .setSeeAlso(concepts.firstNonNull { it.seeAlso })
            .setIsReplacedBy(concepts.firstNonNull { it.isReplacedBy })
            .setReplaces(concepts.firstNonNull { it.replaces })
            .setValidFromIncluding(concepts.firstNonNull { it.validFromIncluding })
            .setValidToIncluding(concepts.firstNonNull { it.validToIncluding })
            .setAssociativeRelation(concepts.firstNonNull { it.associativeRelation })
            .setPartitiveRelation(concepts.firstNonNull { it.partitiveRelation })
            .setGenericRelation(concepts.firstNonNull { it.genericRelation })
            .setCreated(concepts.firstNonNull { it.created })
            .setExactMatch(concepts.firstNonNull { it.exactMatch })
            .setCloseMatch(concepts.firstNonNull { it.closeMatch })
            .setMemberOf(concepts.firstNonNull { it.memberOf })
            .setRemark(concepts.firstNonNull { it.remark })
            .setRange(concepts.firstNonNull { it.range })
            .build()
    }

    /**
     * Convenience overload for merging a prioritized concept with fallback concepts.
     *
     * @param prioritized The highest priority concept
     * @param fallbacks Additional concepts in priority order
     * @return A new Concept with values from the highest priority non-null source
     */
    fun merge(
        prioritized: Concept,
        vararg fallbacks: Concept,
    ): Concept = merge(listOf(prioritized) + fallbacks)

    /**
     * Finds the first non-null value for a field across the prioritized concepts.
     */
    private inline fun <T> List<Concept>.firstNonNull(extractor: (Concept) -> T?): T? = this.firstNotNullOfOrNull(extractor)
}
