package no.digdir.fdk.parseservice.namespace

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class EUAT {

    companion object {
        const val uri = "http://publications.europa.eu/ontology/authority/"

        val authorityCode: Property = ResourceFactory.createProperty("${uri}authority-code")
    }

}
