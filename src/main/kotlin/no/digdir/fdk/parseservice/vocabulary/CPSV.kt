package no.digdir.fdk.parseservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class CPSV {

    companion object {
        const val uri = "http://purl.org/vocab/cpsv#"

        val follows: Property = ResourceFactory.createProperty("${uri}follows")
        val implements: Property = ResourceFactory.createProperty("${uri}implements")
    }

}
