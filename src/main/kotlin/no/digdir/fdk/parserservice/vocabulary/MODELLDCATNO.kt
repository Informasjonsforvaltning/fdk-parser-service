package no.digdir.fdk.parserservice.vocabulary

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class MODELLDCATNO {
    companion object {
        const val URI = "https://data.norge.no/vocabulary/modelldcatno#"

        val informationModelIdentifier: Property = ResourceFactory.createProperty("${URI}informationModelIdentifier")
        val containsModelElement: Property = ResourceFactory.createProperty("${URI}containsModelElement")
        val hasProperty: Property = ResourceFactory.createProperty("${URI}hasProperty")
        val belongsToModule: Property = ResourceFactory.createProperty("${URI}belongsToModule")
        val hasType: Property = ResourceFactory.createProperty("${URI}hasType")
        val isAbstractionOf: Property = ResourceFactory.createProperty("${URI}isAbstractionOf")
        val refersTo: Property = ResourceFactory.createProperty("${URI}refersTo")
        val hasDataType: Property = ResourceFactory.createProperty("${URI}hasDataType")
        val hasSimpleType: Property = ResourceFactory.createProperty("${URI}hasSimpleType")
        val hasObjectType: Property = ResourceFactory.createProperty("${URI}hasObjectType")
        val hasValueFrom: Property = ResourceFactory.createProperty("${URI}hasValueFrom")
        val hasSome: Property = ResourceFactory.createProperty("${URI}hasSome")
        val hasMember: Property = ResourceFactory.createProperty("${URI}hasMember")
        val contains: Property = ResourceFactory.createProperty("${URI}contains")
        val hasSupplier: Property = ResourceFactory.createProperty("${URI}hasSupplier")
        val hasGeneralConcept: Property = ResourceFactory.createProperty("${URI}hasGeneralConcept")
        val formsSymmetryWith: Property = ResourceFactory.createProperty("${URI}formsSymmetryWith")
        val minOccurs: Property = ResourceFactory.createProperty("${URI}minOccurs")
        val maxOccurs: Property = ResourceFactory.createProperty("${URI}maxOccurs")
        val model: Property = ResourceFactory.createProperty("${URI}model")
        val relationPropertyLabel: Property = ResourceFactory.createProperty("${URI}relationPropertyLabel")
        val sequenceNumber: Property = ResourceFactory.createProperty("${URI}sequenceNumber")
        val notification: Property = ResourceFactory.createProperty("${URI}notification")
        val codeListReference: Property = ResourceFactory.createProperty("${URI}codeListReference")
        val typeDefinitionReference: Property = ResourceFactory.createProperty("${URI}typeDefinitionReference")

        val InformationModel: Resource = ResourceFactory.createResource("${URI}InformationModel")
        val CodeElement: Resource = ResourceFactory.createResource("${URI}CodeElement")
        val CodeList: Resource = ResourceFactory.createResource("${URI}CodeList")
        val SimpleType: Resource = ResourceFactory.createResource("${URI}SimpleType")
    }
}
