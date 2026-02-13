package no.digdir.fdk.parserservice.parser.dataset

import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.model.dataset.DatasetType
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.dataset.extractListOfDatasetsInSeries
import no.digdir.fdk.parserservice.extract.dataset.extractListOfDistributionsV3
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.ADMS
import no.digdir.fdk.parserservice.vocabulary.DCAT3
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Parser implementation for HVD-DCAT-AP-NO (High Value Datasets).
 *
 * This parser handles the parsing of High Value Datasets according to the Norwegian
 * HVD-DCAT-AP-NO specification, which supplements DCAT-AP-NO v3.0 with HVD-specific
 * requirements from the EU High Value Datasets Implementing Regulation (EU) 2023/138.
 *
 * @see <a href="https://data.norge.no/specification/hvd-dcat-ap-no">HVD-DCAT-AP-NO Specification</a>
 * @see <a href="https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX%3A32023R0138">HVD Implementing Regulation</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component(value = "DatasetHvdDcatApNoParser")
class HvdDcatApNoParser : BaseDatasetParser() {
    /**
     * Gets the default language for HVD-DCAT-AP-NO.
     *
     * @return "no" (Norwegian)
     */
    override fun getDefaultLanguage(): String = LanguageCodes.NORWEGIAN.code

    /**
     * Gets the version string for this parser.
     *
     * @return "1.0.2" (current HVD-DCAT-AP-NO version)
     */
    override fun getVersion(): String = "1.0.2"

    /**
     * Gets the source format identifier.
     *
     * @return "HVD-DCAT-AP-NO"
     */
    override fun getSourceFormat(): String = "HVD-DCAT-AP-NO"

    /**
     * Gets the acceptable RDF types for datasets.
     *
     * Accepts both regular datasets and dataset series, as both can be HVDs.
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
     * Parses an RDF model into a Dataset object according to HVD-DCAT-AP-NO.
     *
     * This method extracts both standard DCAT-AP-NO v3.0 metadata and HVD-specific metadata.
     *
     * @param model The Jena RDF model containing the dataset
     * @param iri The IRI of the dataset resource
     * @param fdkId The FDK identifier for the dataset (optional)
     * @return The parsed Dataset object with HVD-specific metadata
     * @throws NoAcceptableTypesException if the resource is not a dataset or dataset series
     * @throws IllegalArgumentException if the model is null or invalid
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
        builder.addCommonV3DatasetValues(datasetResource)

        builder.setDistribution(datasetResource.extractListOfDistributionsV3(DCAT.distribution))
        builder.setSample(datasetResource.extractListOfDistributionsV3(ADMS.sample))

        if (model.containsTriple(datasetResource.uri, RDF.type.uri, URI.create(DCAT3.DatasetSeries.uri))) {
            builder.setSpecializedType(DatasetType.datasetSeries)
            builder.setLast(datasetResource.extractStringValue(DCAT3.last))
            builder.setDatasetsInSeries(datasetResource.extractListOfDatasetsInSeries())
        } else {
            builder.setSpecializedType(null)
            builder.setLast(null)
            builder.setDatasetsInSeries(null)
        }

        // Properties not implemented in HVD-DCAT-AP-NO
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
