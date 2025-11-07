package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class DQV {
    companion object {
        const val URI = "http://www.w3.org/ns/dqv#"

        val inDimension: Property = ResourceFactory.createProperty("${URI}inDimension")
        val hasQualityAnnotation: Property = ResourceFactory.createProperty("${URI}hasQualityAnnotation")
    }
}
