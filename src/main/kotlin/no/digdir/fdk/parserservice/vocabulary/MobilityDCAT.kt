package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class MobilityDCAT {

    companion object {
        const val uri = "https://w3id.org/mobilitydcat-ap#"

        val mobilityTheme: Property = ResourceFactory.createProperty("${uri}mobilityTheme")
    }

}
