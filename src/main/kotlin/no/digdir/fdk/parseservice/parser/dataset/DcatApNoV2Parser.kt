package no.digdir.fdk.parseservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.extract.dataset.extractListOfDistributionsV2
import no.digdir.fdk.parseservice.extract.extractListOfTemporal
import no.digdir.fdk.parseservice.extract.fdk.addFdkData
import no.digdir.fdk.parseservice.extract.fdk.fdkRecord
import no.digdir.fdk.parseservice.extract.fdk.primaryTopicFromFdkRecord
import no.digdir.fdk.parseservice.extract.singleResource
import no.digdir.fdk.parseservice.model.LanguageCodes
import no.digdir.fdk.parseservice.vocabulary.ADMS
import no.digdir.fdk.parseservice.vocabulary.DCAT3
import no.digdir.fdk.parseservice.vocabulary.SCHEMA
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Parser implementation for DCAT-AP-NO version 2.2.
 * 
 * This parser handles the parsing of datasets according to the Norwegian
 * Data Catalog Application Profile (DCAT-AP-NO) version 2.2 specification.
 * 
 * The parser extracts dataset metadata including distributions, themes,
 * temporal information, and other properties defined in the DCAT-AP-NO v1.1
 * specification.
 * 
 * ## Usage Example
 * 
 * ```kotlin
 * val parser = DcatApNoV2Parser()
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
 * @see <a href="https://data.norge.no/specification/dcat-ap-no/v2.2">DCAT-AP-NO v2.2 Specification</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class DcatApNoV2Parser(
    @Value("\${fdk.parser.patterns.datasetURI}") val uriPattern: String
) : BaseDatasetParser() {
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
     * Gets the URI pattern for FDK records.
     * 
     * @return dataset URI pattern
     */
    override fun getFDKURIPattern(): String = uriPattern

    /**
     * Gets the acceptable RDF types for datasets.
     * 
     * @return List containing DCAT.Dataset
     */
    override fun getAcceptableTypes(): List<Resource> = listOf(DCAT.Dataset)

    /**
     * Parses an RDF model into a Dataset object according to DCAT-AP-NO v2.2.
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

        builder.setTemporal(datasetResource.extractListOfTemporal(DCTerms.temporal, DCAT.startDate, DCAT.endDate))
        builder.setDistribution(datasetResource.extractListOfDistributionsV2(DCAT.distribution))
        builder.setSample(datasetResource.extractListOfDistributionsV2(ADMS.sample))

        val lasDataset = datasetResource.singleResource(DCAT3.last)
        builder.setLast(lasDataset?.takeIf { it.isURIResource }?.uri)
        builder.setPrev(null)

        builder.setHasRelevanceAnnotation(null)
        builder.setHasCurrentnessAnnotation(null)
        builder.setHasCompletenessAnnotation(null)
        builder.setHasAvailabilityAnnotation(null)
        builder.setHasAccuracyAnnotation(null)
        builder.setQualifiedAttributions(null)
        builder.setInSeries(null)
        builder.setDatasetsInSeries(null)
        builder.setLegalBasisForProcessing(null)
        builder.setLegalBasisForRestriction(null)
        builder.setLegalBasisForAccess(null)
        builder.setSpecializedType(null)

        return builder.build()
    }
}
