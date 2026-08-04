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
            .setId(concepts.firstNotNullOfOrNull { it.id })
            .setUri(concepts.firstNotNullOfOrNull { it.uri })
            .setIdentifier(concepts.firstNotNullOfOrNull { it.identifier })
            .setHarvest(concepts.firstNotNullOfOrNull { it.harvest })
            .setType(concepts.firstNotNullOfOrNull { it.type })
            .setCollection(concepts.firstNotNullOfOrNull { it.collection })
            .setPublisher(concepts.firstNotNullOfOrNull { it.publisher })
            .setCreator(concepts.firstNotNullOfOrNull { it.creator })
            .setSubject(concepts.firstNotNullOfOrNull { it.subject })
            .setStatus(concepts.firstNotNullOfOrNull { it.status })
            .setExample(concepts.firstNotNullOfOrNull { it.example })
            .setPrefLabel(concepts.firstNotNullOfOrNull { it.prefLabel })
            .setHiddenLabel(concepts.firstNotNullOfOrNull { it.hiddenLabel })
            .setAltLabel(concepts.firstNotNullOfOrNull { it.altLabel })
            .setContactPoint(concepts.firstNotNullOfOrNull { it.contactPoint })
            .setDefinition(concepts.firstNotNullOfOrNull { it.definition })
            .setDefinitions(concepts.firstNotNullOfOrNull { it.definitions })
            .setSeeAlso(concepts.firstNotNullOfOrNull { it.seeAlso })
            .setIsReplacedBy(concepts.firstNotNullOfOrNull { it.isReplacedBy })
            .setReplaces(concepts.firstNotNullOfOrNull { it.replaces })
            .setValidFromIncluding(concepts.firstNotNullOfOrNull { it.validFromIncluding })
            .setValidToIncluding(concepts.firstNotNullOfOrNull { it.validToIncluding })
            .setAssociativeRelation(concepts.firstNotNullOfOrNull { it.associativeRelation })
            .setPartitiveRelation(concepts.firstNotNullOfOrNull { it.partitiveRelation })
            .setGenericRelation(concepts.firstNotNullOfOrNull { it.genericRelation })
            .setCreated(concepts.firstNotNullOfOrNull { it.created })
            .setExactMatch(concepts.firstNotNullOfOrNull { it.exactMatch })
            .setCloseMatch(concepts.firstNotNullOfOrNull { it.closeMatch })
            .setMemberOf(concepts.firstNotNullOfOrNull { it.memberOf })
            .setRemark(concepts.firstNotNullOfOrNull { it.remark })
            .setRange(concepts.firstNotNullOfOrNull { it.range })
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
}
