package no.digdir.fdk.parseservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.vocabulary.DCAT

class DCAT3 {

    companion object {
        const val uri = DCAT.NS

        val prev: Property = ResourceFactory.createProperty("${uri}prev")
        val last: Property = ResourceFactory.createProperty("${uri}last")
    }

}
