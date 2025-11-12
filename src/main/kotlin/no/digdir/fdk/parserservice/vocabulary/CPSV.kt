package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class CPSV {
    companion object {
        const val URI = "http://purl.org/vocab/cpsv#"

        val follows: Property = ResourceFactory.createProperty("${URI}follows")
        val hasInput: Property = ResourceFactory.createProperty("${URI}hasInput")
        val implements: Property = ResourceFactory.createProperty("${URI}implements")
        val produces: Property = ResourceFactory.createProperty("${URI}produces")

        val PublicService: Resource = ResourceFactory.createResource("${URI}PublicService")
    }
}
