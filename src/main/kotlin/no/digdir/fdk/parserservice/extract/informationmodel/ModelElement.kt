package no.digdir.fdk.parserservice.extract.informationmodel

import no.digdir.fdk.model.informationmodel.ModelCodeElement
import no.digdir.fdk.model.informationmodel.ModelElement
import no.digdir.fdk.parserservice.extract.*
import no.digdir.fdk.parserservice.vocabulary.MODELLDCATNO
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS
import org.apache.jena.vocabulary.XSD

private fun ModelElement.hasContent() = when {
    uri != null -> true
    identifier != null -> true
    title != null -> true
    description != null -> true
    else -> false
}

private fun ModelCodeElement.hasContent() = when {
    uri != null -> true
    identifier != null -> true
    prefLabel != null -> true
    else -> false
}

private fun Resource.buildModelCodeElement(): ModelCodeElement? {
    val builder = ModelCodeElement.newBuilder()

    builder.setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setPrefLabel(extractLocalizedStrings(SKOS.prefLabel))
        .setInScheme(extractListOfStrings(SKOS.inScheme))
        .setSubject(extractStringValue(DCTerms.subject))
        .setNotation(extractStringValue(SKOS.notation))
        .setTopConceptOf(extractListOfStrings(SKOS.topConceptOf))
        .setDefinition(extractListOfStrings(SKOS.definition))
        .setExample(extractListOfStrings(SKOS.example))
        .setExclusionNote(extractLocalizedStrings(SKOS.scopeNote))
        .setPreviousElement(extractListOfStrings(SKOS.broader))
        .setHiddenLabel(extractLocalizedStrings(SKOS.hiddenLabel))
        .setInclusionNote(extractLocalizedStrings(SKOS.scopeNote))
        .setNote(extractLocalizedStrings(SKOS.note))
        .setNextElement(extractListOfStrings(SKOS.narrower))
        .setScopeNote(extractLocalizedStrings(SKOS.scopeNote))
        .setAltLabel(extractLocalizedStrings(SKOS.altLabel))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.buildModelElement(): ModelElement? {
    val builder = ModelElement.newBuilder()

    builder.setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setTitle(extractLocalizedStrings(DCTerms.title))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setSubject(extractStringValue(DCTerms.subject))
        .setHasProperty(extractListOfStrings(MODELLDCATNO.hasProperty))
        .setBelongsToModule(extractStringValue(MODELLDCATNO.belongsToModule))
        .setElementTypes(extractListOfStrings(RDF.type))
        .setCodeListReference(extractStringValue(MODELLDCATNO.codeListReference))
        .setCodes(listResources(SKOS.member)
            ?.mapNotNull { it.buildModelCodeElement() }
            ?.takeIf { it.isNotEmpty() })
        .setTypeDefinitionReference(extractStringValue(MODELLDCATNO.typeDefinitionReference))
        .setFractionDigits(extractIntegerValue(XSD.fractionDigits))
        .setLength(extractIntegerValue(XSD.length))
        .setMaxInclusive(extractDoubleValue(XSD.maxInclusive))
        .setMaxLength(extractIntegerValue(XSD.maxLength))
        .setMinInclusive(extractDoubleValue(XSD.minInclusive))
        .setMinLength(extractIntegerValue(XSD.minLength))
        .setPattern(extractStringValue(XSD.pattern))
        .setTotalDigits(extractIntegerValue(XSD.totalDigits))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.extractListOfModelElements(): List<ModelElement>? =
    listResources(MODELLDCATNO.containsModelElement)
        ?.mapNotNull { it.buildModelElement() }
        ?.takeIf { it.isNotEmpty() }

