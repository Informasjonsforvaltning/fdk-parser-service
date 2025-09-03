package no.digdir.fdk.parseservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.dataset.extractListOfDistributionsV1
import no.digdir.fdk.parseservice.extract.fdk.addFdkData
import no.digdir.fdk.parseservice.extract.fdk.fdkRecord
import no.digdir.fdk.parseservice.extract.fdk.primaryTopicFromFdkRecord
import no.digdir.fdk.parseservice.model.LanguageCodes
import no.digdir.fdk.parseservice.vocabulary.ADMS
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT

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
class DcatApNoV1Parser : BaseDatasetParser() {
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
     * Gets the URI pattern for FDK records.
     * 
     * @return "fellesdatakatalog.digdir.no"
     */
    override fun getFDKURIPattern(): String = "fellesdatakatalog.digdir.no"

    /**
     * Gets the acceptable RDF types for datasets.
     * 
     * @return List containing DCAT.Dataset
     */
    override fun getAcceptableTypes(): List<Resource> = listOf(DCAT.Dataset)

    /**
     * Parses an RDF model into a Dataset object according to DCAT-AP-NO v1.1.
     * 
     * This method extracts the FDK record, identifies the primary topic (dataset),
     * and builds a complete Dataset object with all available metadata.
     * 
     * @param model The Jena RDF model containing the dataset
     * @return The parsed Dataset object
     * @throws IllegalArgumentException if the model is null or invalid
     * @throws UnsupportedOperationException if no valid FDK record is found
     */
    override fun parse(model: Model): Dataset {
        val recordResource = fdkRecord(model, getAcceptableTypes(), getFDKURIPattern())
        val datasetResource = primaryTopicFromFdkRecord(recordResource, getAcceptableTypes())

        val builder = Dataset.newBuilder()

        builder.addFdkData(recordResource, datasetResource)

        builder.addCommonDatasetValues(datasetResource)

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
