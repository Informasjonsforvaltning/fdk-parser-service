package no.digdir.fdk.parseservice.vocabulary

import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class DQVISO {

    companion object {
        const val uri = "http://iso.org/25012/2008/dataquality/"

        val Accuracy: Resource = ResourceFactory.createResource("${uri}Accuracy")
        val Availability: Resource = ResourceFactory.createResource("${uri}Availability")
        val Completeness: Resource = ResourceFactory.createResource("${uri}Completeness")
        val Currentness: Resource = ResourceFactory.createResource("${uri}Currentness")
        val Relevance: Resource = ResourceFactory.createResource("${uri}Relevance")
    }

}
