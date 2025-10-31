package no.digdir.fdk.parserservice.parser.informationmodel

import no.digdir.fdk.model.informationmodel.InformationModel
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.MODELLDCATNO
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component
import java.net.URI

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
class ModellDcatApNoV1Parser() : BaseInformationModelParser() {
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

    override fun parse(model: Model, iri: String): InformationModel =
        parseInformationModel(model, iri, null)

    override fun parse(model: Model, iri: String, fdkId: String): InformationModel =
        parseInformationModel(model, iri, fdkId)

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
    private fun parseInformationModel(model: Model, iri: String, fdkId: String?): InformationModel {
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

        return builder.build()
    }
}
