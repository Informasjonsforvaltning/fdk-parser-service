package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class MobilityDCAT {
    companion object {
        const val URI = "https://w3id.org/mobilitydcat-ap#"

        val mobilityTheme: Property = ResourceFactory.createProperty("${URI}mobilityTheme")
        val mobilityDataStandard: Property = ResourceFactory.createProperty("${URI}mobilityDataStandard")
    }
}
