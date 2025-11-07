package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class CPSV {
    companion object {
        const val URI = "http://purl.org/vocab/cpsv#"

        val follows: Property = ResourceFactory.createProperty("${URI}follows")
        val implements: Property = ResourceFactory.createProperty("${URI}implements")
    }
}
