package no.digdir.fdk.parseservice.namespace

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class FDK {

    companion object {
        const val uri =
            "https://raw.githubusercontent.com/Informasjonsforvaltning/fdk-reasoning-service/main/src/main/resources/ontology/fdk.owl#"

        val isAuthoritative: Property = ResourceFactory.createProperty("${uri}isAuthoritative")
        val isRelatedToTransportportal: Property = ResourceFactory.createProperty("${uri}isRelatedToTransportportal")
        val isOpenData: Property = ResourceFactory.createProperty("${uri}isOpenData")
    }

}
