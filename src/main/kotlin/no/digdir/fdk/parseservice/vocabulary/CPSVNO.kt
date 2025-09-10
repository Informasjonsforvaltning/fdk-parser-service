package no.digdir.fdk.parseservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class CPSVNO {

    companion object {
        const val uri = "https://data.norge.no/vocabulary/cpsvno#"

        val ruleForDisclosure: Property = ResourceFactory.createProperty("${uri}ruleForDisclosure")
        val ruleForNonDisclosure: Property = ResourceFactory.createProperty("${uri}ruleForNonDisclosure")
        val ruleForDataProcessing: Property = ResourceFactory.createProperty("${uri}ruleForDataProcessing")
    }

}
