package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class DCATNO {
    companion object {
        const val URI = "https://data.norge.no/vocabulary/dcatno#"

        val containsEvent: Property = ResourceFactory.createProperty("${URI}containsEvent")
        val containsService: Property = ResourceFactory.createProperty("${URI}containsService")
    }
}
