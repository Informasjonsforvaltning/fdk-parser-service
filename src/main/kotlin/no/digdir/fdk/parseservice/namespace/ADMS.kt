package no.digdir.fdk.parseservice.namespace

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class ADMS {

    companion object {
        const val uri =
            "http://www.w3.org/ns/adms#"

        val identifier: Property = ResourceFactory.createProperty("${uri}identifier")
        val sample: Property = ResourceFactory.createProperty("${uri}sample")
    }

}
