package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class CV {
    companion object {
        const val URI = "http://data.europa.eu/m8g/"

        val contactPage: Property = ResourceFactory.createProperty("${URI}contactPage")
        val contactPoint: Property = ResourceFactory.createProperty("${URI}contactPoint")
        val currency: Property = ResourceFactory.createProperty("${URI}currency")
        val email: Property = ResourceFactory.createProperty("${URI}email")
        val fulfils: Property = ResourceFactory.createProperty("${URI}fulfils")
        val hasChannel: Property = ResourceFactory.createProperty("${URI}hasChannel")
        val hasCompetentAuthority: Property = ResourceFactory.createProperty("${URI}hasCompetentAuthority")
        val hasCost: Property = ResourceFactory.createProperty("${URI}hasCost")
        val hasLegalResource: Property = ResourceFactory.createProperty("${URI}hasLegalResource")
        val hasParticipant: Property = ResourceFactory.createProperty("${URI}hasParticipant")
        val hasParticipation: Property = ResourceFactory.createProperty("${URI}hasParticipation")
        val holdsRequirement: Property = ResourceFactory.createProperty("${URI}holdsRequirement")
        val hasValue: Property = ResourceFactory.createProperty("${URI}hasValue")
        val ifAccessedThrough: Property = ResourceFactory.createProperty("${URI}ifAccessedThrough")
        val isClassifiedBy: Property = ResourceFactory.createProperty("${URI}isClassifiedBy")
        val isDescribedAt: Property = ResourceFactory.createProperty("${URI}isDescribedAt")
        val isGroupedBy: Property = ResourceFactory.createProperty("${URI}isGroupedBy")
        val ownedBy: Property = ResourceFactory.createProperty("${URI}ownedBy")
        val processingTime: Property = ResourceFactory.createProperty("${URI}processingTime")
        val relatedService: Property = ResourceFactory.createProperty("${URI}relatedService")
        val role: Property = ResourceFactory.createProperty("${URI}role")
        val sector: Property = ResourceFactory.createProperty("${URI}sector")
        val telephone: Property = ResourceFactory.createProperty("${URI}telephone")
        val thematicArea: Property = ResourceFactory.createProperty("${URI}thematicArea")
        val value: Property = ResourceFactory.createProperty("${URI}value")

        val Event: Resource = ResourceFactory.createResource("${URI}Event")
        val BusinessEvent: Resource = ResourceFactory.createResource("${URI}BusinessEvent")
        val LifeEvent: Resource = ResourceFactory.createResource("${URI}LifeEvent")
        val PublicOrganisation: Resource = ResourceFactory.createResource("${URI}PublicOrganisation")
    }
}
