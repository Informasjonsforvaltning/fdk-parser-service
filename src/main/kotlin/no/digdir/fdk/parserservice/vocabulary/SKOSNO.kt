package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class SKOSNO {
    companion object {
        const val URI = "https://data.norge.no/vocabulary/skosno#"

        val valueRange: Property = ResourceFactory.createProperty("${URI}valueRange")
        val relationshipWithSource: Property = ResourceFactory.createProperty("${URI}relationshipWithSource")

        val isFromConceptIn: Property = ResourceFactory.createProperty("${URI}isFromConceptIn")
        val relationRole: Property = ResourceFactory.createProperty("${URI}relationRole")
        val hasToConcept: Property = ResourceFactory.createProperty("${URI}hasToConcept")
        val hasPartitiveConceptRelation: Property = ResourceFactory.createProperty("${URI}hasPartitiveConceptRelation")
        val hasPartitiveConcept: Property = ResourceFactory.createProperty("${URI}hasPartitiveConcept")
        val hasComprehensiveConcept: Property = ResourceFactory.createProperty("${URI}hasComprehensiveConcept")
        val hasGenericConceptRelation: Property = ResourceFactory.createProperty("${URI}hasGenericConceptRelation")
        val hasSpecificConcept: Property = ResourceFactory.createProperty("${URI}hasSpecificConcept")
        val hasGenericConcept: Property = ResourceFactory.createProperty("${URI}hasGenericConcept")

        val definisjon: Property = ResourceFactory.createProperty("${URI}definisjon")
        val forholdTilKilde: Property = ResourceFactory.createProperty("${URI}forholdTilKilde")
        val assosiativRelasjon: Property = ResourceFactory.createProperty("${URI}assosiativRelasjon")
        val generiskRelasjon: Property = ResourceFactory.createProperty("${URI}generiskRelasjon")
        val partitivRelasjon: Property = ResourceFactory.createProperty("${URI}partitivRelasjon")
    }
}
