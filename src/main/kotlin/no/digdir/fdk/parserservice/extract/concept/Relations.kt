package no.digdir.fdk.parserservice.extract.concept

import no.digdir.fdk.model.concept.ConceptAssociativeRelation
import no.digdir.fdk.model.concept.ConceptGenericRelation
import no.digdir.fdk.model.concept.ConceptPartitiveRelation
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.SKOSNO
import no.digdir.fdk.parserservice.vocabulary.XKOS
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS

/**
 * Extracts associative relations from the concept.
 *
 * @receiver The RDF resource representing the concept
 * @return a list of `ConceptAssociativeRelation` instances or `null` if no associative relations are found
 * @see ConceptAssociativeRelation
 */
fun Resource.extractAssociativeRelations(): List<ConceptAssociativeRelation>? {
    val relations = mutableListOf<ConceptAssociativeRelation>()

    listResources(SKOSNO.isFromConceptIn)?.forEach { relResource ->
        relations.add(
            ConceptAssociativeRelation
                .newBuilder()
                .setDescription(relResource.extractLocalizedStrings(SKOSNO.relationRole))
                .setRelated(relResource.extractStringValue(SKOSNO.hasToConcept))
                .build(),
        )
    }

    return relations.takeIf { it.isNotEmpty() }
}

/**
 * Extracts associative relations from the concept using a deprecated approach.
 *
 * This method is used for SKOS-AP-NO v1.1.1 compatibility.
 *
 * @receiver The RDF resource representing the concept
 * @return A list of `ConceptAssociativeRelation` instances, or `null` if no relations are found
 * @see ConceptAssociativeRelation
 * @see extractAssociativeRelations
 */
fun Resource.extractAssociativeRelationsV1(): List<ConceptAssociativeRelation>? {
    val relations = mutableListOf<ConceptAssociativeRelation>()

    listResources(SKOSNO.assosiativRelasjon)?.forEach { relResource ->
        relations.add(
            ConceptAssociativeRelation
                .newBuilder()
                .setDescription(relResource.extractLocalizedStrings(DCTerms.description))
                .setRelated(relResource.extractStringValue(SKOS.related))
                .build(),
        )
    }

    return relations.takeIf { it.isNotEmpty() }
}

/**
 * Extracts partitive relations from the concept.
 *
 * @receiver The RDF resource representing the concept
 * @return a list of `ConceptPartitiveRelation` instances or `null` if no partitive relations are found
 * @see ConceptPartitiveRelation
 */
fun Resource.extractPartitiveRelations(): List<ConceptPartitiveRelation>? {
    val relations = mutableListOf<ConceptPartitiveRelation>()

    listResources(SKOSNO.hasPartitiveConceptRelation)?.forEach { relResource ->
        relations.add(
            ConceptPartitiveRelation
                .newBuilder()
                .setDescription(relResource.extractLocalizedStrings(DCTerms.description))
                .setHasPart(relResource.extractStringValue(SKOSNO.hasPartitiveConcept))
                .setIsPartOf(relResource.extractStringValue(SKOSNO.hasComprehensiveConcept))
                .build(),
        )
    }

    return relations.takeIf { it.isNotEmpty() }
}

/**
 * Extracts partitive relations from the concept using a deprecated approach.
 *
 * This method is used for SKOS-AP-NO v1.1.1 compatibility.
 *
 * @receiver The RDF resource representing the concept
 * @return A list of `ConceptPartitiveRelation` instances, or `null` if no relations are found
 * @see ConceptPartitiveRelation
 * @see extractPartitiveRelations
 */
fun Resource.extractPartitiveRelationsV1(): List<ConceptPartitiveRelation>? {
    val relations = mutableListOf<ConceptPartitiveRelation>()

    listResources(SKOSNO.partitivRelasjon)?.forEach { relResource ->
        relations.add(
            ConceptPartitiveRelation
                .newBuilder()
                .setDescription(relResource.extractLocalizedStrings(DCTerms.description))
                .setHasPart(relResource.extractStringValue(DCTerms.hasPart))
                .setIsPartOf(relResource.extractStringValue(DCTerms.isPartOf))
                .build(),
        )
    }

    return relations.takeIf { it.isNotEmpty() }
}

/**
 * Extracts generic relations from the concept.
 *
 * @receiver The RDF resource representing the concept
 * @return a list of `ConceptGenericRelation` instances or `null` if no generic relations are found
 * @see ConceptGenericRelation
 */
fun Resource.extractGenericRelations(): List<ConceptGenericRelation>? {
    val relations = mutableListOf<ConceptGenericRelation>()

    listResources(SKOSNO.hasGenericConceptRelation)?.forEach { relResource ->
        relations.add(
            ConceptGenericRelation
                .newBuilder()
                .setDivisioncriterion(relResource.extractLocalizedStrings(DCTerms.description))
                .setGeneralizes(relResource.extractStringValue(SKOSNO.hasSpecificConcept))
                .setSpecializes(relResource.extractStringValue(SKOSNO.hasGenericConcept))
                .build(),
        )
    }

    return relations.takeIf { it.isNotEmpty() }
}

/**
 * Extracts generic relations from the concept using a deprecated approach.
 *
 * This method is used for SKOS-AP-NO v1.1.1 compatibility.
 *
 * @receiver The RDF resource representing the concept
 * @return A list of `ConceptGenericRelation` instances, or `null` if no relations are found
 * @see ConceptGenericRelation
 * @see extractGenericRelations
 */
fun Resource.extractGenericRelationsV1(): List<ConceptGenericRelation>? {
    val relations = mutableListOf<ConceptGenericRelation>()

    listResources(SKOSNO.generiskRelasjon)?.forEach { relResource ->
        relations.add(
            ConceptGenericRelation
                .newBuilder()
                .setDivisioncriterion(relResource.extractLocalizedStrings(DCTerms.description))
                .setGeneralizes(relResource.extractStringValue(XKOS.generalizes))
                .setSpecializes(relResource.extractStringValue(XKOS.specializes))
                .build(),
        )
    }

    return relations.takeIf { it.isNotEmpty() }
}
