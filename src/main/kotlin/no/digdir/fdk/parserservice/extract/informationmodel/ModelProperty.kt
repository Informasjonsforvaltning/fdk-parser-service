package no.digdir.fdk.parserservice.extract.informationmodel

import no.digdir.fdk.model.informationmodel.InformationModelProperty
import no.digdir.fdk.parserservice.extract.extractIntegerValue
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractListOfUriOrIdentifier
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.extractUriOrIdentifier
import no.digdir.fdk.parserservice.vocabulary.MODELLDCATNO
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF

private fun InformationModelProperty.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        title != null -> true
        description != null -> true
        subject != null -> true
        propertyTypes != null -> true
        minOccurs != null -> true
        maxOccurs != null -> true
        hasType != null -> true
        relationPropertyLabel != null -> true
        sequenceNumber != null -> true
        belongsToModule != null -> true
        formsSymmetryWith != null -> true
        isAbstractionOf != null -> true
        refersTo != null -> true
        hasDataType != null -> true
        hasSimpleType != null -> true
        hasObjectType != null -> true
        hasValueFrom != null -> true
        hasSome != null -> true
        hasMember != null -> true
        contains != null -> true
        hasSupplier != null -> true
        hasGeneralConcept != null -> true
        notification != null -> true
        else -> false
    }

fun Resource.buildModelProperty(): InformationModelProperty? {
    val builder = InformationModelProperty.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setTitle(extractLocalizedStrings(DCTerms.title))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setSubject(extractStringValue(DCTerms.subject))
        .setPropertyTypes(extractListOfStrings(RDF.type))
        .setMinOccurs(extractIntegerValue(MODELLDCATNO.minOccurs))
        .setMaxOccurs(extractIntegerValue(MODELLDCATNO.maxOccurs))
        .setHasType(extractListOfUriOrIdentifier(MODELLDCATNO.hasType))
        .setRelationPropertyLabel(extractLocalizedStrings(MODELLDCATNO.relationPropertyLabel))
        .setSequenceNumber(extractIntegerValue(MODELLDCATNO.sequenceNumber))
        .setBelongsToModule(extractStringValue(MODELLDCATNO.belongsToModule))
        .setFormsSymmetryWith(extractUriOrIdentifier(MODELLDCATNO.formsSymmetryWith))
        .setIsAbstractionOf(extractUriOrIdentifier(MODELLDCATNO.isAbstractionOf))
        .setRefersTo(extractUriOrIdentifier(MODELLDCATNO.refersTo))
        .setHasDataType(extractUriOrIdentifier(MODELLDCATNO.hasDataType))
        .setHasSimpleType(extractUriOrIdentifier(MODELLDCATNO.hasSimpleType))
        .setHasObjectType(extractUriOrIdentifier(MODELLDCATNO.hasObjectType))
        .setHasValueFrom(extractUriOrIdentifier(MODELLDCATNO.hasValueFrom))
        .setHasSome(extractListOfUriOrIdentifier(MODELLDCATNO.hasSome))
        .setHasMember(extractUriOrIdentifier(MODELLDCATNO.hasMember))
        .setContains(extractUriOrIdentifier(MODELLDCATNO.contains))
        .setHasSupplier(extractUriOrIdentifier(MODELLDCATNO.hasSupplier))
        .setHasGeneralConcept(extractUriOrIdentifier(MODELLDCATNO.hasGeneralConcept))
        .setNotification(extractLocalizedStrings(MODELLDCATNO.notification))

    return builder.build().takeIf { it.hasContent() }
}
