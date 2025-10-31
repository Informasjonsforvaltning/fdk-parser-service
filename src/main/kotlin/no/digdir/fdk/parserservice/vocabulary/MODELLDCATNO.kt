package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class MODELLDCATNO {

    companion object {
        const val uri = "https://data.norge.no/vocabulary/modelldcatno#"

        val informationModelIdentifier: Property = ResourceFactory.createProperty("${uri}informationModelIdentifier")
        val containsModelElement: Property = ResourceFactory.createProperty("${uri}containsModelElement")
        val hasProperty: Property = ResourceFactory.createProperty("${uri}hasProperty")
        val belongsToModule: Property = ResourceFactory.createProperty("${uri}belongsToModule")
        val hasType: Property = ResourceFactory.createProperty("${uri}hasType")
        val isAbstractionOf: Property = ResourceFactory.createProperty("${uri}isAbstractionOf")
        val refersTo: Property = ResourceFactory.createProperty("${uri}refersTo")
        val hasDataType: Property = ResourceFactory.createProperty("${uri}hasDataType")
        val hasSimpleType: Property = ResourceFactory.createProperty("${uri}hasSimpleType")
        val hasObjectType: Property = ResourceFactory.createProperty("${uri}hasObjectType")
        val hasValueFrom: Property = ResourceFactory.createProperty("${uri}hasValueFrom")
        val hasSome: Property = ResourceFactory.createProperty("${uri}hasSome")
        val hasMember: Property = ResourceFactory.createProperty("${uri}hasMember")
        val contains: Property = ResourceFactory.createProperty("${uri}contains")
        val hasSupplier: Property = ResourceFactory.createProperty("${uri}hasSupplier")
        val hasGeneralConcept: Property = ResourceFactory.createProperty("${uri}hasGeneralConcept")
        val formsSymmetryWith: Property = ResourceFactory.createProperty("${uri}formsSymmetryWith")
        val minOccurs: Property = ResourceFactory.createProperty("${uri}minOccurs")
        val maxOccurs: Property = ResourceFactory.createProperty("${uri}maxOccurs")
        val relationPropertyLabel: Property = ResourceFactory.createProperty("${uri}relationPropertyLabel")
        val sequenceNumber: Property = ResourceFactory.createProperty("${uri}sequenceNumber")
        val notification: Property = ResourceFactory.createProperty("${uri}notification")
        val codeListReference: Property = ResourceFactory.createProperty("${uri}codeListReference")
        val typeDefinitionReference: Property = ResourceFactory.createProperty("${uri}typeDefinitionReference")

        val InformationModel: Resource = ResourceFactory.createResource("${uri}InformationModel")
        val ObjectType: Resource = ResourceFactory.createResource("${uri}ObjectType")
        val Attribute: Resource = ResourceFactory.createResource("${uri}Attribute")
        val Role: Resource = ResourceFactory.createResource("${uri}Role")
    }

}
