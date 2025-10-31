package no.digdir.fdk.parserservice.extract.informationmodel

import no.digdir.fdk.model.informationmodel.InformationModelCodeElement
import no.digdir.fdk.model.informationmodel.InformationModelElement
import no.digdir.fdk.parserservice.extract.extractDoubleValue
import no.digdir.fdk.parserservice.extract.extractIntegerValue
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractListOfUriOrIdentifier
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.extractURIStringValue
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.vocabulary.MODELLDCATNO
import no.digdir.fdk.parserservice.vocabulary.XKOS
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS
import org.apache.jena.vocabulary.XSD

private fun InformationModelElement.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        title != null -> true
        description != null -> true
        subject != null -> true
        hasProperty != null -> true
        belongsToModule != null -> true
        elementTypes != null -> true
        codeListReference != null -> true
        codes != null -> true
        typeDefinitionReference != null -> true
        fractionDigits != null -> true
        length != null -> true
        maxInclusive != null -> true
        maxLength != null -> true
        minInclusive != null -> true
        minLength != null -> true
        pattern != null -> true
        totalDigits != null -> true
        else -> false
    }

private fun InformationModelCodeElement.hasContent() =
    when {
        uri != null -> true
        identifier != null -> true
        prefLabel != null -> true
        inScheme != null -> true
        subject != null -> true
        notation != null -> true
        topConceptOf != null -> true
        definition != null -> true
        example != null -> true
        exclusionNote != null -> true
        previousElement != null -> true
        hiddenLabel != null -> true
        inclusionNote != null -> true
        note != null -> true
        nextElement != null -> true
        scopeNote != null -> true
        altLabel != null -> true
        else -> false
    }

private fun Resource.buildModelCodeElement(): InformationModelCodeElement? {
    val builder = InformationModelCodeElement.newBuilder()

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setPrefLabel(extractLocalizedStrings(SKOS.prefLabel))
        .setInScheme(extractListOfStrings(SKOS.inScheme))
        .setSubject(extractStringValue(DCTerms.subject))
        .setNotation(extractStringValue(SKOS.notation))
        .setTopConceptOf(extractListOfStrings(SKOS.topConceptOf))
        .setDefinition(extractListOfStrings(SKOS.definition))
        .setExample(extractListOfStrings(SKOS.example))
        .setExclusionNote(extractLocalizedStrings(XKOS.exclusionNote))
        .setPreviousElement(extractListOfStrings(XKOS.previous))
        .setHiddenLabel(extractLocalizedStrings(SKOS.hiddenLabel))
        .setInclusionNote(extractLocalizedStrings(XKOS.inclusionNote))
        .setNote(extractLocalizedStrings(SKOS.note))
        .setNextElement(extractListOfStrings(XKOS.next))
        .setScopeNote(extractLocalizedStrings(SKOS.scopeNote))
        .setAltLabel(extractLocalizedStrings(SKOS.altLabel))

    return builder.build().takeIf { it.hasContent() }
}

fun Resource.buildModelElement(): InformationModelElement? {
    val builder = InformationModelElement.newBuilder()

    val elementTypes = listResources(RDF.type)

    builder
        .setUri(extractURIStringValue())
        .setIdentifier(extractStringValue(DCTerms.identifier))
        .setTitle(extractLocalizedStrings(DCTerms.title))
        .setDescription(extractLocalizedStrings(DCTerms.description))
        .setSubject(extractStringValue(DCTerms.subject))
        .setHasProperty(extractListOfUriOrIdentifier(MODELLDCATNO.hasProperty))
        .setBelongsToModule(extractStringValue(MODELLDCATNO.belongsToModule))
        .setElementTypes(extractListOfStrings(RDF.type))

    when {
        elementTypes == null -> ignoreTypeSpecificFields(builder)
        elementTypes.contains(MODELLDCATNO.CodeList) -> addCodeListFields(builder)
        elementTypes.contains(MODELLDCATNO.SimpleType) -> addSimpleTypeFields(builder)
        else -> ignoreTypeSpecificFields(builder)
    }

    return builder.build().takeIf { it.hasContent() }
}

private fun ignoreTypeSpecificFields(builder: InformationModelElement.Builder) {
    // The following fields are not relevant for default type elements
    builder
        .setCodeListReference(null)
        .setCodes(null)
        .setTypeDefinitionReference(null)
        .setFractionDigits(null)
        .setLength(null)
        .setMaxInclusive(null)
        .setMaxLength(null)
        .setMinInclusive(null)
        .setMinLength(null)
        .setPattern(null)
        .setTotalDigits(null)
}

private fun Resource.addCodeListFields(builder: InformationModelElement.Builder) {
    builder
        .setCodeListReference(extractStringValue(MODELLDCATNO.codeListReference))
        .setCodes(
            model
                .listResourcesWithProperty(SKOS.inScheme, this)
                .asSequence()
                .filter { it.hasProperty(RDF.type, MODELLDCATNO.CodeElement) }
                .mapNotNull { it.buildModelCodeElement() }
                .toList()
                .takeIf { it.isNotEmpty() },
        )

    // The following fields are not relevant for code list elements
    builder
        .setTypeDefinitionReference(null)
        .setFractionDigits(null)
        .setLength(null)
        .setMaxInclusive(null)
        .setMaxLength(null)
        .setMinInclusive(null)
        .setMinLength(null)
        .setPattern(null)
        .setTotalDigits(null)
}

private fun Resource.addSimpleTypeFields(builder: InformationModelElement.Builder) {
    builder
        .setTypeDefinitionReference(extractStringValue(MODELLDCATNO.typeDefinitionReference))
        .setFractionDigits(extractIntegerValue(XSD.fractionDigits))
        .setLength(extractIntegerValue(XSD.length))
        .setMaxInclusive(extractDoubleValue(XSD.maxInclusive))
        .setMaxLength(extractIntegerValue(XSD.maxLength))
        .setMinInclusive(extractDoubleValue(XSD.minInclusive))
        .setMinLength(extractIntegerValue(XSD.minLength))
        .setPattern(extractStringValue(XSD.pattern))
        .setTotalDigits(extractIntegerValue(XSD.totalDigits))

    // The following fields are not relevant for simple type elements
    builder
        .setCodeListReference(null)
        .setCodes(null)
}
