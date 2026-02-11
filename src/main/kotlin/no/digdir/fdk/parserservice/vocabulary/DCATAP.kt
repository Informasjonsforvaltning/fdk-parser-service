package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class DCATAP {
    companion object {
        const val URI = "http://data.europa.eu/r5r/"

        val applicableLegislation: Property = ResourceFactory.createProperty("${URI}applicableLegislation")
    }
}
