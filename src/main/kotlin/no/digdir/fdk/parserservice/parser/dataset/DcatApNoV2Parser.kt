package no.digdir.fdk.parserservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.model.dataset.DatasetType
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.dataset.extractInSeries
import no.digdir.fdk.parserservice.extract.dataset.extractListOfDatasetsInSeries
import no.digdir.fdk.parserservice.extract.dataset.extractListOfDistributionsV2
import no.digdir.fdk.parserservice.extract.dataset.extractListOfLegalBasisV2
import no.digdir.fdk.parserservice.extract.dataset.extractListOfQualifiedAttributions
import no.digdir.fdk.parserservice.extract.dataset.extractQualityAnnotation
import no.digdir.fdk.parserservice.extract.extractListOfTemporal
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.ADMS
import no.digdir.fdk.parserservice.vocabulary.CPSVNO
import no.digdir.fdk.parserservice.vocabulary.DCAT3
import no.digdir.fdk.parserservice.vocabulary.DQVISO
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component
import java.net.URI

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
class DcatApNoV2Parser() : BaseDatasetParser() {
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
     * Gets the acceptable RDF types for datasets.
     * 
     * @return List containing DCAT.Dataset
     */
    override fun getAcceptableTypes(): List<Resource> = listOf(DCAT.Dataset, DCAT3.DatasetSeries)

    override fun parse(model: Model, iri: String): Dataset =
        parseDataset(model, iri, null)

    override fun parse(model: Model, iri: String, fdkId: String): Dataset =
        parseDataset(model, iri, fdkId)

    /**
     * Parses an RDF model into a Dataset object according to DCAT-AP-NO v2.2.
     *
     * This method extracts the FDK record when fdkId is present
     * and builds a complete Dataset object with all available metadata.
     *
     * @param model The Jena RDF model containing the dataset
     * @param iri The IRI of the dataset
     * @param fdkId The FDK ID of the dataset
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

        builder.setTemporal(datasetResource.extractListOfTemporal(DCTerms.temporal, DCAT.startDate, DCAT.endDate))
        builder.setDistribution(datasetResource.extractListOfDistributionsV2(DCAT.distribution))
        builder.setSample(datasetResource.extractListOfDistributionsV2(ADMS.sample))

        builder.setPrev(datasetResource.extractStringValue(DCAT3.prev))
        builder.setInSeries(datasetResource.extractInSeries())

        builder.setHasRelevanceAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Relevance))
        builder.setHasCurrentnessAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Currentness))
        builder.setHasCompletenessAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Completeness))
        builder.setHasAvailabilityAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Availability))
        builder.setHasAccuracyAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Accuracy))

        builder.setQualifiedAttributions(datasetResource.extractListOfQualifiedAttributions())

        builder.setLegalBasisForProcessing(datasetResource.extractListOfLegalBasisV2(CPSVNO.ruleForDataProcessing))
        builder.setLegalBasisForRestriction(datasetResource.extractListOfLegalBasisV2(CPSVNO.ruleForNonDisclosure))
        builder.setLegalBasisForAccess(datasetResource.extractListOfLegalBasisV2(CPSVNO.ruleForDisclosure))

        if (model.containsTriple(datasetResource.uri, RDF.type.uri, URI.create(DCAT3.DatasetSeries.uri))) {
            builder.setSpecializedType(DatasetType.datasetSeries)
            builder.setLast(datasetResource.extractStringValue(DCAT3.last))
            builder.setDatasetsInSeries(datasetResource.extractListOfDatasetsInSeries())
        } else {
            builder.setSpecializedType(null)
            builder.setLast(null)
            builder.setDatasetsInSeries(null)
        }

        // The following properties are not implemented in DCAT-AP-NO v2.2
        builder.setMobilityTheme(null)

        return builder.build()
    }
}
