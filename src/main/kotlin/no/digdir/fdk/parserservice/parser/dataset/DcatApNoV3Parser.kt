package no.digdir.fdk.parserservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.model.dataset.DatasetType
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.dataset.extractInSeries
import no.digdir.fdk.parserservice.extract.dataset.extractListOfDatasetsInSeries
import no.digdir.fdk.parserservice.extract.dataset.extractListOfDistributionsV3
import no.digdir.fdk.parserservice.extract.dataset.extractListOfQualifiedAttributions
import no.digdir.fdk.parserservice.extract.dataset.extractListOfQualityAnnotations
import no.digdir.fdk.parserservice.extract.extractListOfLegalResources
import no.digdir.fdk.parserservice.extract.extractListOfTemporal
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.ADMS
import no.digdir.fdk.parserservice.vocabulary.DCAT3
import no.digdir.fdk.parserservice.vocabulary.DCATAP
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Parser implementation for DCAT-AP-NO version 3.
 *
 * This parser handles the parsing of datasets according to the Norwegian
 * Data Catalog Application Profile (DCAT-AP-NO) version 3.0.6 specification.
 *
 * The parser extracts dataset metadata including distributions, themes,
 * temporal information, quality annotations, legal basis, and other properties
 * defined in the DCAT-AP-NO v3.0 specification.
 *
 * @see <a href="https://data.norge.no/specification/dcat-ap-no">DCAT-AP-NO Specification</a>
 * @see <a href="https://www.w3.org/TR/vocab-dcat-3/">DCAT Version 3</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component(value = "DatasetDcatApNoV3Parser")
class DcatApNoV3Parser : BaseDatasetParser() {
    /**
     * Gets the default language for DCAT-AP-NO v3.0.
     *
     * @return "no" (Norwegian)
     */
    override fun getDefaultLanguage(): String = LanguageCodes.NORWEGIAN.code

    /**
     * Gets the version string for this parser.
     *
     * @return "3.0.6"
     */
    override fun getVersion(): String = "3.0.6"

    /**
     * Gets the source format identifier.
     *
     * @return "DCAT-AP-NO"
     */
    override fun getSourceFormat(): String = "DCAT-AP-NO"

    /**
     * Gets the acceptable RDF types for datasets.
     *
     * Accepts both regular datasets and dataset series.
     *
     * @return List containing DCAT.Dataset and DCAT3.DatasetSeries
     */
    override fun getAcceptableTypes(): List<Resource> = listOf(DCAT.Dataset, DCAT3.DatasetSeries)

    override fun parse(
        model: Model,
        iri: String,
    ): Dataset = parseDataset(model, iri, null)

    override fun parse(
        model: Model,
        iri: String,
        fdkId: String,
    ): Dataset = parseDataset(model, iri, fdkId)

    /**
     * Parses an RDF model into a Dataset object according to DCAT-AP-NO v3.
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
    private fun parseDataset(
        model: Model,
        iri: String,
        fdkId: String?,
    ): Dataset {
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

        builder.setDistribution(datasetResource.extractListOfDistributionsV3(DCAT.distribution))
        builder.setSample(datasetResource.extractListOfDistributionsV3(ADMS.sample))

        builder.setPrev(datasetResource.extractStringValue(DCAT3.prev))
        builder.setInSeries(datasetResource.extractInSeries())

        builder.setQualifiedAttributions(datasetResource.extractListOfQualifiedAttributions())
        builder.setApplicableLegislation(datasetResource.extractListOfLegalResources(DCATAP.applicableLegislation))
        builder.setQualityAnnotations(datasetResource.extractListOfQualityAnnotations())

        if (model.containsTriple(datasetResource.uri, RDF.type.uri, URI.create(DCAT3.DatasetSeries.uri))) {
            builder.setSpecializedType(DatasetType.datasetSeries)
            builder.setLast(datasetResource.extractStringValue(DCAT3.last))
            builder.setDatasetsInSeries(datasetResource.extractListOfDatasetsInSeries())
        } else {
            builder.setSpecializedType(null)
            builder.setLast(null)
            builder.setDatasetsInSeries(null)
        }

        // The following properties are not implemented in DCAT-AP-NO v3
        builder.setMobilityTheme(null)
        builder.setLegalBasisForProcessing(null)
        builder.setLegalBasisForRestriction(null)
        builder.setLegalBasisForAccess(null)
        builder.setHasRelevanceAnnotation(null)
        builder.setHasCurrentnessAnnotation(null)
        builder.setHasCompletenessAnnotation(null)
        builder.setHasAvailabilityAnnotation(null)
        builder.setHasAccuracyAnnotation(null)

        return builder.build()
    }
}
