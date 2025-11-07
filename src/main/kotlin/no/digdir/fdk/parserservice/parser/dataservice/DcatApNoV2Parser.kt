package no.digdir.fdk.parserservice.parser.dataservice

import no.digdir.fdk.model.dataservice.DataService
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Parser implementation for DCAT-AP-NO version 2.2.
 *
 * This parser handles the parsing of data services according to the Norwegian
 * Data Catalog Application Profile (DCAT-AP-NO) version 2.2 specification.
 *
 * The parser extracts data service metadata including endpoints, formats,
 * served datasets, and other properties defined in the DCAT-AP-NO v2.2 specification.
 *
 * ## Usage Example
 *
 * ```kotlin
 * val parser = DcatApNoV2Parser()
 * val model = ModelFactory.createDefaultModel()
 * model.read(inputStream, null, "TURTLE")
 *
 * val dataService = parser.parse(model)
 * println("Data service title: ${dataService.title?.no}")
 * println("Data service URI: ${dataService.uri}")
 * ```
 *
 * ## Supported Properties
 *
 * The parser extracts the following data service properties:
 * - Basic metadata (title, description, identifier)
 * - Publisher and organization information
 * - Endpoint URLs and descriptions
 * - Formats supported by the service
 * - Datasets served by the service
 * - Conformance information
 * - Contact points and access rights
 *
 * @see <a href="https://data.norge.no/specification/dcat-ap-no/v2.2">DCAT-AP-NO v2.2 Specification</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component(value = "DataServiceDcatApNoV2Parser")
class DcatApNoV2Parser : BaseDataServiceParser() {
    /**
     * Gets the default language for DCAT-AP-NO v2.2.
     *
     * @return "no" (Norwegian)
     */
    override fun getDefaultLanguage(): String = LanguageCodes.NORWEGIAN.code

    /**
     * Gets the version string for this parser.
     *
     * @return "2.2"
     */
    override fun getVersion(): String = "2.2"

    /**
     * Gets the source format identifier.
     *
     * @return "DCAT-AP-NO"
     */
    override fun getSourceFormat(): String = "DCAT-AP-NO"

    /**
     * Gets the acceptable RDF types for data services.
     *
     * @return List containing DCAT.DataService
     */
    override fun getAcceptableTypes(): List<Resource> = listOf(DCAT.DataService)

    override fun parse(
        model: Model,
        iri: String,
    ): DataService = parseDataService(model, iri, null)

    override fun parse(
        model: Model,
        iri: String,
        fdkId: String,
    ): DataService = parseDataService(model, iri, fdkId)

    /**
     * Parses an RDF model into a DataService object according to DCAT-AP-NO v2.2.
     *
     * This method extracts the FDK record when fdkId is present
     * and builds a complete DataService object with all available metadata.
     *
     * @param model The Jena RDF model containing the data service
     * @param iri The IRI of the data service
     * @param fdkId The FDK ID of the data service
     * @return The parsed DataService object
     * @throws IllegalArgumentException if the model is null or invalid
     * @throws UnsupportedOperationException if no valid FDK record is found
     */
    private fun parseDataService(
        model: Model,
        iri: String,
        fdkId: String?,
    ): DataService {
        if (getAcceptableTypes().none { model.containsTriple(iri, RDF.type.uri, URI.create(it.uri)) }) {
            throw NoAcceptableTypesException("No acceptable types found for $iri")
        }

        val dataServiceResource = resourceOfIRI(model, iri)

        val builder = DataService.newBuilder()

        if (fdkId != null) {
            val recordResource = fdkRecord(dataServiceResource, fdkId)
            builder.addFdkData(recordResource)
        }

        builder.addCommonDataServiceValues(dataServiceResource)

        return builder.build()
    }
}
