package no.digdir.fdk.parserservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.dataset.extractListOfDistributionsV1
import no.digdir.fdk.parserservice.extract.extractListOfTemporal
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.ADMS
import no.digdir.fdk.parserservice.vocabulary.SCHEMA
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Parser implementation for DCAT-AP-NO version 1.1.
 * 
 * This parser handles the parsing of datasets according to the Norwegian
 * Data Catalog Application Profile (DCAT-AP-NO) version 1.1 specification.
 * 
 * The parser extracts dataset metadata including distributions, themes,
 * temporal information, and other properties defined in the DCAT-AP-NO v1.1
 * specification.
 * 
 * ## Usage Example
 * 
 * ```kotlin
 * val parser = DcatApNoV1Parser()
 * val model = ModelFactory.createDefaultModel()
 * model.read(inputStream, null, "TURTLE")
 * 
 * val dataset = parser.parse(model)
 * println("Dataset title: ${dataset.title?.no}")
 * println("Dataset URI: ${dataset.uri}")
 * ```
 * 
 * ## Supported Properties
 * 
 * The parser extracts the following dataset properties:
 * - Basic metadata (title, description, identifier)
 * - Publisher and organization information
 * - Themes and classifications (EU Data Themes, LOS, EuroVoc)
 * - Temporal information (issued, modified dates)
 * - Distributions and samples
 * - Keywords and access rights
 * - Spatial and provenance information
 * 
 * @see <a href="https://data.norge.no/specification/dcat-ap-no/v1.1">DCAT-AP-NO v1.1 Specification</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class DcatApNoV1Parser() : BaseDatasetParser() {
    /**
     * Gets the default language for DCAT-AP-NO v1.1.
     * 
     * @return "no" (Norwegian)
     */
    override fun getDefaultLanguage(): String = LanguageCodes.NORWEGIAN.code

    /**
     * Gets the version string for this parser.
     * 
     * @return "1.1"
     */
    override fun getVersion(): String = "1.1"

    /**
     * Gets the source format identifier.
     * 
     * @return "DCAT-AP-NO"
     */
    override fun getSourceFormat(): String = "DCAT-AP-NO"

    /**
     * Gets the acceptable RDF types for datasets.
     * 
     * @return List containing DCAT.Dataset
     */
    override fun getAcceptableTypes(): List<Resource> = listOf(DCAT.Dataset)

    override fun parse(model: Model, iri: String): Dataset =
        parseDataset(model, iri, null)

    override fun parse(model: Model, iri: String, fdkId: String): Dataset =
        parseDataset(model, iri, fdkId)

    /**
     * Parses an RDF model into a Dataset object according to DCAT-AP-NO v1.1.
     * 
     * This method extracts the FDK record when fdkId is present
     * and builds a complete Dataset object with all available metadata.
     * 
     * @param model The Jena RDF model containing the dataset
     * @param iri The IRI of the dataset
     * @return The parsed Dataset object
     * @throws IllegalArgumentException if the model is null or invalid
     * @throws UnsupportedOperationException if no valid FDK record is found
     */
    private fun parseDataset(model: Model, iri: String, fdkId: String?): Dataset {
        if (getAcceptableTypes().none { model.containsTriple(iri, RDF.type.uri, URI.create(it.uri)) }) {
            throw NoAcceptableTypesException("No acceptable types found for $iri")
        }

        val datasetResource = resourceOfIRI(model, iri)

        val builder = Dataset.newBuilder()

        if (fdkId != null) {
            val recordResource = fdkRecord(datasetResource, fdkId)
            builder.addFdkData(recordResource, datasetResource)
        }

        builder.addCommonDatasetValues(datasetResource)

        builder.setTemporal(datasetResource.extractListOfTemporal(DCTerms.temporal, SCHEMA.startDate, SCHEMA.endDate))
        builder.setDistribution(datasetResource.extractListOfDistributionsV1(DCAT.distribution))
        builder.setSample(datasetResource.extractListOfDistributionsV1(ADMS.sample))

        // The following properties are not implemented in DCAT-AP-NO v1.1
        builder.setHasRelevanceAnnotation(null)
        builder.setHasCurrentnessAnnotation(null)
        builder.setHasCompletenessAnnotation(null)
        builder.setHasAvailabilityAnnotation(null)
        builder.setHasAccuracyAnnotation(null)
        builder.setQualifiedAttributions(null)
        builder.setInSeries(null)
        builder.setDatasetsInSeries(null)
        builder.setLast(null)
        builder.setPrev(null)
        builder.setLegalBasisForProcessing(null)
        builder.setLegalBasisForRestriction(null)
        builder.setLegalBasisForAccess(null)
        builder.setSpecializedType(null)

        return builder.build()
    }
}
