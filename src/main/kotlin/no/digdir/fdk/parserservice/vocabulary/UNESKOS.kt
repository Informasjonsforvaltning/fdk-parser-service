package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class UNESKOS {
    companion object {
        const val URI = "http://purl.org/umu/uneskos#"

        val memberOf: Property = ResourceFactory.createProperty("${URI}memberOf")
    }
}
