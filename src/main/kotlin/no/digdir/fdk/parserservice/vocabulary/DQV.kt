package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class DQV {

    companion object {
        const val uri = "http://www.w3.org/ns/dqv#"

        val inDimension: Property = ResourceFactory.createProperty("${uri}inDimension")
        val hasQualityAnnotation: Property = ResourceFactory.createProperty("${uri}hasQualityAnnotation")
    }

}
