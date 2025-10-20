package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class EULANG {

    companion object {
        const val uri = "http://publications.europa.eu/resource/authority/language/"

        val NOR: Resource = ResourceFactory.createResource("${uri}NOR")
        val NOB: Resource = ResourceFactory.createResource("${uri}NOB")
        val NNO: Resource = ResourceFactory.createResource("${uri}NNO")
        val ENG: Resource = ResourceFactory.createResource("${uri}ENG")
    }

}
