package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class EULANG {
    companion object {
        const val URI = "http://publications.europa.eu/resource/authority/language/"

        val NOR: Resource = ResourceFactory.createResource("${URI}NOR")
        val NOB: Resource = ResourceFactory.createResource("${URI}NOB")
        val NNO: Resource = ResourceFactory.createResource("${URI}NNO")
        val ENG: Resource = ResourceFactory.createResource("${URI}ENG")
    }
}
