package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.vocabulary.DCAT

class DCAT3 {

    companion object {
        const val uri = DCAT.NS

        val prev: Property = ResourceFactory.createProperty("${uri}prev")
        val last: Property = ResourceFactory.createProperty("${uri}last")
        val inSeries: Property = ResourceFactory.createProperty("${uri}inSeries")

        val DatasetSeries: Resource = ResourceFactory.createResource("${uri}DatasetSeries")
    }

}
