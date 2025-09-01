package no.digdir.fdk.parseservice.namespace

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class FDKORG {

    companion object {
        const val uri =
            "https://raw.githubusercontent.com/Informasjonsforvaltning/organization-catalog/main/src/main/resources/ontology/organization-catalog.owl#"

        val orgPath: Property = ResourceFactory.createProperty("${uri}orgPath")
    }

}
