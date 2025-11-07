package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.vocabulary.DCAT

class DCAT3 {
    companion object {
        const val URI = DCAT.NS

        val prev: Property = ResourceFactory.createProperty("${URI}prev")
        val last: Property = ResourceFactory.createProperty("${URI}last")
        val inSeries: Property = ResourceFactory.createProperty("${URI}inSeries")

        val DatasetSeries: Resource = ResourceFactory.createResource("${URI}DatasetSeries")
    }
}
