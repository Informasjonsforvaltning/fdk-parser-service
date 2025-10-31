package no.digdir.fdk.parserservice.parser.informationmodel

import no.digdir.fdk.model.informationmodel.InformationModel
import no.digdir.fdk.model.informationmodel.InformationModelElement
import no.digdir.fdk.model.informationmodel.InformationModelProperty
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.extract.informationmodel.buildModelElement
import no.digdir.fdk.parserservice.extract.informationmodel.buildModelProperty
import no.digdir.fdk.parserservice.extract.listResources
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.MODELLDCATNO
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component
import java.net.URI
import kotlin.collections.mutableMapOf

/**
 * Parser implementation for ModellDCAT-AP-NO.
 *
 * This parser handles the parsing of information models according to the Norwegian
 * Model Data Catalog Application Profile (ModellDCAT-AP-NO) specification.
 *
 * The parser extracts information model metadata including model elements, properties,
 * and other properties defined in the ModellDCAT-AP-NO specification.
 *
 * ## Usage Example
 *
 * ```kotlin
 * val parser = ModellDcatApNoV1Parser()
 * val model = ModelFactory.createDefaultModel()
 * model.read(inputStream, null, "TURTLE")
 *
 * val infoModel = parser.parse(model)
 * println("Information model title: ${infoModel.title?.no}")
 * println("Information model URI: ${infoModel.uri}")
 * ```
 *
 * ## Supported Properties
 *
 * The parser extracts the following information model properties:
 * - Basic metadata (title, description, identifier)
 * - Publisher and organization information
 * - Model elements and properties
 * - Conformance information
 * - Contact points and access rights
 * - Licensing and spatial/temporal coverage
 *
 * @see <a href="https://data.norge.no/specification/modelldcat-ap-no">ModellDCAT-AP-NO Specification</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class ModellDcatApNoV1Parser : BaseInformationModelParser() {
    /**
     * Gets the default language for ModellDCAT-AP-NO.
     *
     * @return "no" (Norwegian)
     */
    override fun getDefaultLanguage(): String = LanguageCodes.NORWEGIAN.code

    /**
     * Gets the version string for this parser.
     *
     * @return "1.3.2"
     */
    override fun getVersion(): String = "1.3.2"

    /**
     * Gets the source format identifier.
     *
     * @return "ModellDCAT-AP-NO"
     */
    override fun getSourceFormat(): String = "ModellDCAT-AP-NO"

    /**
     * Gets the acceptable RDF types for information models.
     *
     * @return List containing MODELDCATNO.InformationModel
     */
    override fun getAcceptableTypes(): List<Resource> = listOf(MODELLDCATNO.InformationModel)

    override fun parse(
        model: Model,
        iri: String,
    ): InformationModel = parseInformationModel(model, iri, null)

    override fun parse(
        model: Model,
        iri: String,
        fdkId: String,
    ): InformationModel = parseInformationModel(model, iri, fdkId)

    private val modelElementsMap = mutableMapOf<CharSequence, InformationModelElement>()
    private val modelPropertiesMap = mutableMapOf<CharSequence, InformationModelProperty>()
    private val containsSubjects = mutableSetOf<CharSequence>()

    /**
     * Parses an RDF model into an InformationModel object according to ModelDCAT-AP-NO.
     *
     * This method extracts the FDK record when fdkId is present
     * and builds a complete InformationModel object with all available metadata.
     *
     * @param model The Jena RDF model containing the information model
     * @param iri The IRI of the information model
     * @param fdkId The FDK ID of the information model
     * @return The parsed InformationModel object
     * @throws IllegalArgumentException if the model is null or invalid
     * @throws UnsupportedOperationException if no valid FDK record is found
     */
    private fun parseInformationModel(
        model: Model,
        iri: String,
        fdkId: String?,
    ): InformationModel {
        if (getAcceptableTypes().none { model.containsTriple(iri, RDF.type.uri, URI.create(it.uri)) }) {
            throw NoAcceptableTypesException("No acceptable types found for $iri")
        }

        val infoModelResource = resourceOfIRI(model, iri)

        val builder = InformationModel.newBuilder()

        if (fdkId != null) {
            val recordResource = fdkRecord(infoModelResource, fdkId)
            builder.addFdkData(recordResource)
        }

        builder.addCommonInformationModelValues(infoModelResource)

        // Remove any remaining subjects, elements or properties from previous parse events
        modelElementsMap.clear()
        modelPropertiesMap.clear()
        containsSubjects.clear()

        // Extract model subjects, elements and properties
        val modelSubjects = infoModelResource.extractListOfStrings(DCTerms.subject)

        builder.setSubjects(modelSubjects)
        if (modelSubjects != null) containsSubjects.addAll(modelSubjects)

        infoModelResource
            .listResources(MODELLDCATNO.containsModelElement)
            ?.forEach { it.addElementToMap() }

        builder.setModelElements(modelElementsMap.takeIf { it.isNotEmpty() })
        builder.setModelProperties(modelPropertiesMap.takeIf { it.isNotEmpty() })
        builder.setContainsModelElements(modelElementsMap.keys.toList().takeIf { it.isNotEmpty() })
        builder.setContainsSubjects(containsSubjects.toList().takeIf { it.isNotEmpty() })

        return builder.build()
    }

    private fun Resource.addElementToMap() {
        val element = buildModelElement()
        if (element != null) {
            val elementKey = element.uri ?: element.identifier
            if (elementKey != null && !modelElementsMap.containsKey(elementKey)) {
                modelElementsMap[elementKey] = element

                // Add subjects to set of subjects contained in the model
                if (element.subject != null) containsSubjects.add(element.subject)
                element.codes?.forEach { code ->
                    if (code.subject != null) containsSubjects.add(code.subject)
                }

                // Extract properties from element
                listResources(MODELLDCATNO.hasProperty)
                    ?.forEach { it.addPropertyToMap() }
            }
        }
    }

    private fun Resource.addPropertyToMap() {
        val property = buildModelProperty()
        if (property != null) {
            val propertyKey = property.uri ?: property.identifier
            if (propertyKey != null && !modelPropertiesMap.containsKey(propertyKey)) {
                modelPropertiesMap[propertyKey] = property

                // Add subject to set of subjects contained in the model
                if (property.subject != null) containsSubjects.add(property.subject)

                // Extract properties from property
                listResources(MODELLDCATNO.formsSymmetryWith)
                    ?.forEach { it.addPropertyToMap() }

                // Extract elements from property
                listResources(MODELLDCATNO.hasType)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.isAbstractionOf)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.refersTo)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.hasDataType)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.hasSimpleType)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.hasObjectType)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.hasValueFrom)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.hasSome)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.hasMember)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.contains)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.hasSupplier)
                    ?.forEach { it.addElementToMap() }
                listResources(MODELLDCATNO.hasGeneralConcept)
                    ?.forEach { it.addElementToMap() }
            }
        }
    }
}
