package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class XKOS {
    companion object {
        const val URI = "http://rdf-vocabulary.ddialliance.org/xkos#"

        val exclusionNote: Property = ResourceFactory.createProperty("${URI}exclusionNote")
        val inclusionNote: Property = ResourceFactory.createProperty("${URI}inclusionNote")
        val next: Property = ResourceFactory.createProperty("${URI}next")
        val previous: Property = ResourceFactory.createProperty("${URI}previous")
    }
}
