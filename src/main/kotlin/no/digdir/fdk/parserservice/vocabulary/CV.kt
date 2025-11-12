package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.ResourceFactory

class CV {
    companion object {
        const val URI = "http://data.europa.eu/m8g/"

        val contactPoint: Property = ResourceFactory.createProperty("${URI}contactPoint")
        val ownedBy: Property = ResourceFactory.createProperty("${URI}ownedBy")
        val isGroupedBy: Property = ResourceFactory.createProperty("${URI}isGroupedBy")
        val sector: Property = ResourceFactory.createProperty("${URI}sector")
        val isClassifiedBy: Property = ResourceFactory.createProperty("${URI}isClassifiedBy")
        val isDescribedAt: Property = ResourceFactory.createProperty("${URI}isDescribedAt")
        val hasParticipation: Property = ResourceFactory.createProperty("${URI}hasParticipation")
        val hasLegalResource: Property = ResourceFactory.createProperty("${URI}hasLegalResource")
        val hasChannel: Property = ResourceFactory.createProperty("${URI}hasChannel")
        val processingTime: Property = ResourceFactory.createProperty("${URI}processingTime")
        val thematicArea: Property = ResourceFactory.createProperty("${URI}thematicArea")
        val hasCompetentAuthority: Property = ResourceFactory.createProperty("${URI}hasCompetentAuthority")
    }
}
