package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class DQVISO {
    companion object {
        const val URI = "http://iso.org/25012/2008/dataquality/"

        val Accuracy: Resource = ResourceFactory.createResource("${URI}Accuracy")
        val Availability: Resource = ResourceFactory.createResource("${URI}Availability")
        val Completeness: Resource = ResourceFactory.createResource("${URI}Completeness")
        val Currentness: Resource = ResourceFactory.createResource("${URI}Currentness")
        val Relevance: Resource = ResourceFactory.createResource("${URI}Relevance")
    }
}
