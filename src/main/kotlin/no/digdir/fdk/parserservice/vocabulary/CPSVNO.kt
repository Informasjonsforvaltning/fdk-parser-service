package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class CPSVNO {
    companion object {
        const val URI = "https://data.norge.no/vocabulary/cpsvno#"

        val ruleForDisclosure: Property = ResourceFactory.createProperty("${URI}ruleForDisclosure")
        val ruleForNonDisclosure: Property = ResourceFactory.createProperty("${URI}ruleForNonDisclosure")
        val ruleForDataProcessing: Property = ResourceFactory.createProperty("${URI}ruleForDataProcessing")

        val Service: Resource = ResourceFactory.createResource("${URI}Service")
    }
}
