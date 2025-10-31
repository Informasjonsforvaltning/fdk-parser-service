package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class PROF {
    companion object {
        const val URI = "https://www.w3.org/ns/dx/prof/"

        val isProfileOf: Property = ResourceFactory.createProperty("${URI}isProfileOf")
    }
}
