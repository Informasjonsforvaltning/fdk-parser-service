package no.digdir.fdk.parserservice.extract.informationmodel

import no.digdir.fdk.model.informationmodel.ModelProperty
import no.digdir.fdk.parserservice.extract.*
import no.digdir.fdk.parserservice.vocabulary.MODELLDCATNO
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF

private fun ModelProperty.hasContent() = when {
    uri != null -> true
    identifier != null -> true
    title != null -> true
    description != null -> true
    else -> false
}

fun Resource.buildModelProperty(): ModelProperty? {
    val builder = ModelProperty.newBuilder()

    builder.setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setTitle(extractLocalizedStrings(DCTerms.title))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setSubject(extractStringValue(DCTerms.subject))
        .setPropertyTypes(extractListOfStrings(RDF.type))
        .setMinOccurs(extractIntegerValue(MODELLDCATNO.minOccurs))
        .setMaxOccurs(extractIntegerValue(MODELLDCATNO.maxOccurs))
        .setHasType(extractListOfStrings(MODELLDCATNO.hasType))
        .setRelationPropertyLabel(extractLocalizedStrings(MODELLDCATNO.relationPropertyLabel))
        .setSequenceNumber(extractIntegerValue(MODELLDCATNO.sequenceNumber))
        .setBelongsToModule(extractStringValue(MODELLDCATNO.belongsToModule))
        .setFormsSymmetryWith(extractStringValue(MODELLDCATNO.formsSymmetryWith))
        .setIsAbstractionOf(extractStringValue(MODELLDCATNO.isAbstractionOf))
        .setRefersTo(extractStringValue(MODELLDCATNO.refersTo))
        .setHasDataType(extractStringValue(MODELLDCATNO.hasDataType))
        .setHasSimpleType(extractStringValue(MODELLDCATNO.hasSimpleType))
        .setHasObjectType(extractStringValue(MODELLDCATNO.hasObjectType))
        .setHasValueFrom(extractStringValue(MODELLDCATNO.hasValueFrom))
        .setHasSome(extractListOfStrings(MODELLDCATNO.hasSome))
        .setHasMember(extractStringValue(MODELLDCATNO.hasMember))
        .setContains(extractStringValue(MODELLDCATNO.contains))
        .setHasSupplier(extractStringValue(MODELLDCATNO.hasSupplier))
        .setHasGeneralConcept(extractStringValue(MODELLDCATNO.hasGeneralConcept))
        .setNotification(extractLocalizedStrings(MODELLDCATNO.notification))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.extractListOfModelProperties(): List<ModelProperty>? =
    listResources(MODELLDCATNO.hasProperty)
        ?.mapNotNull { it.buildModelProperty() }
        ?.takeIf { it.isNotEmpty() }

