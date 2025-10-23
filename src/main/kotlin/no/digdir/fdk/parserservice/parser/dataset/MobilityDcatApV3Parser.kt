package no.digdir.fdk.parserservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.dataset.extractListOfMobilityDistributions
import no.digdir.fdk.parserservice.extract.dataset.extractListOfMobilitySampleData
import no.digdir.fdk.parserservice.extract.dataset.extractListOfQualifiedAttributions
import no.digdir.fdk.parserservice.extract.dataset.extractQualityAnnotation
import no.digdir.fdk.parserservice.extract.extractListOfReferenceDataCodes
import no.digdir.fdk.parserservice.extract.extractListOfTemporal
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.DQVISO
import no.digdir.fdk.parserservice.vocabulary.MobilityDCAT
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS
import org.springframework.stereotype.Component
import java.net.URI


/**
 * Parser implementation for mobilityDCAT-AP version 3.0.0.
 *
 * This parser handles the parsing of datasets according to the Mobility
 * Data Catalog Application Profile (mobilityDCAT-AP) version 3.0.0 specification.
 * It extends the base dataset parser to provide specialized handling for
 * mobility-related datasets.
 *
 * ## Usage Example
 *
 * ```kotlin
 * val parser = MobilityDcatApV3Parser()
 * val model = ModelFactory.createDefaultModel()
 * model.read(inputStream, null, "TURTLE")
 *
 * val dataset = parser.parse(model)
 * println("Dataset mobility theme: ${dataset.mobilityTheme}")
 * println("Dataset URI: ${dataset.uri}")
 * ```
 *
 * ## Supported Properties
 *
 * The parser extracts the following dataset properties:
 * - Basic metadata (title, description, identifier)
 * - Publisher and organization information
 * - Mobility themes and classifications
 * - Temporal information (issued, modified dates)
 * - Mobility-specific distributions and samples
 * - Quality annotations (accuracy, availability, etc.)
 * - Qualified attributions
 *
 * @see <a href="https://mobilitydcat-ap.github.io/mobilityDCAT-AP/releases/3.0.0/index.html">Mobility DCAT-AP Specification</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class MobilityDcatApV3Parser() : BaseDatasetParser() {
    /**
     * Gets the default language for Mobility DCAT-AP v3.0.0.
     *
     * @return "no" (Norwegian)
     */
    override fun getDefaultLanguage(): String = LanguageCodes.NORWEGIAN.code

    /**
     * Gets the version string for this parser.
     *
     * @return "3.0.0"
     */
    override fun getVersion(): String = "3.0.0"

    /**
     * Gets the source format identifier.
     *
     * @return "mobilityDCAT-AP"
     */
    override fun getSourceFormat(): String = "mobilityDCAT-AP"

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
     * Parses an RDF model into a Dataset object according to Mobility DCAT-AP v3.0.0.
     *
     * This method extracts the FDK record when fdkId is present
     * and builds a complete Dataset object with all available metadata.
     * It includes specialized handling for mobility themes and distributions.
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

        builder.setMobilityTheme(datasetResource.extractListOfReferenceDataCodes(MobilityDCAT.mobilityTheme, "/", SKOS.prefLabel))
        builder.setTemporal(datasetResource.extractListOfTemporal(DCTerms.temporal, DCAT.startDate, DCAT.endDate))

        builder.setDistribution(datasetResource.extractListOfMobilityDistributions())
        builder.setSample(datasetResource.extractListOfMobilitySampleData())

        builder.setHasRelevanceAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Relevance))
        builder.setHasCurrentnessAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Currentness))
        builder.setHasCompletenessAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Completeness))
        builder.setHasAvailabilityAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Availability))
        builder.setHasAccuracyAnnotation(datasetResource.extractQualityAnnotation(DQVISO.Accuracy))
        builder.setQualifiedAttributions(datasetResource.extractListOfQualifiedAttributions())

        // The following properties are not implemented in mobilityDCAT-AP v3.0.0
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
