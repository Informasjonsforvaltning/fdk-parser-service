package no.digdir.fdk.parseservice.vocabulary

import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class EUVOC {

    companion object {
        const val uri = "http://publications.europa.eu/ontology/euvoc#"

        val FileType: Resource = ResourceFactory.createResource("${uri}FileType")
    }

}
