package no.digdir.fdk.parseservice.handler

import com.fasterxml.jackson.databind.JsonNode
import no.digdir.fdk.model.dataset.Dataset
import no.digdir.fdk.parseservice.LOGGER
import no.digdir.fdk.parseservice.extract.fdk.topicUriOfRecordWithID
import no.digdir.fdk.parseservice.model.NoAcceptableFDKRecordsException
import no.digdir.fdk.parseservice.model.UnableToParseException
import no.digdir.fdk.parseservice.parser.dataset.DcatApNoV1Parser
import no.digdir.fdk.parseservice.parser.dataset.DcatApNoV2Parser
import no.digdir.fdk.parseservice.parser.dataset.MobilityDcatApV3Parser
import no.digdir.fdk.parseservice.utils.avroToJson
import no.digdir.fdk.parseservice.utils.readTurtle
import org.apache.jena.rdf.model.ModelFactory
import org.springframework.stereotype.Service

@Service
class DatasetHandler(
    private val v1Parser: DcatApNoV1Parser,
    private val v2Parser: DcatApNoV2Parser,
    private val mobilityParser: MobilityDcatApV3Parser
) {

    fun parseDataset(fdkId: String, graph: String): JsonNode {
        val model = ModelFactory.createDefaultModel()
        val dataset: Dataset = try {
            model.readTurtle(graph)
            val resourceIRI = topicUriOfRecordWithID(fdkId, model)
            if (resourceIRI != null) {
                val v1 = try {
                    v1Parser.parse(model, resourceIRI, fdkId)
                } catch (e: Exception)  {
                    LOGGER.warn("Failed to parse v1 for $fdkId", e)
                    null
                }

                val v2 = try {
                    v2Parser.parse(model, resourceIRI, fdkId)
                } catch (e: Exception)  {
                    LOGGER.warn("Failed to parse v2 for $fdkId", e)
                    null
                }

                val mobility = try {
                    mobilityParser.parse(model, resourceIRI, fdkId)
                } catch (e: Exception)  {
                    LOGGER.warn("Failed to parse mobility for $fdkId", e)
                    null
                }

                if (v1 == null && v2 == null && mobility == null) {
                    throw UnableToParseException("Unable to parse any dataset version for $fdkId")
                }

                val combinedV1V2 = combineDatasetResults(
                    prioritized = v2 ?: Dataset(),
                    backup = v1 ?: Dataset()
                )

                if (combinedV1V2.isRelatedToTransportportal) combineDatasetResults(
                    prioritized = mobility ?: Dataset(),
                    backup = combinedV1V2
                ) else combinedV1V2
            } else {
                throw NoAcceptableFDKRecordsException("No dataset found with identifier '$fdkId'")
            }
        } finally {
            model.close()
        }

        return avroToJson(dataset, dataset.schema)
    }

    private fun combineDatasetResults(prioritized: Dataset, backup: Dataset): Dataset =
        Dataset.newBuilder()
            .setId(prioritized.id ?: backup.id)
            .setUri(prioritized.uri ?: backup.uri)
            .setIdentifier(prioritized.identifier ?: backup.identifier)
            .setAdmsIdentifier(prioritized.admsIdentifier ?: backup.admsIdentifier)
            .setHarvest(prioritized.harvest ?: backup.harvest)
            .setCatalog(prioritized.catalog ?: backup.catalog)
            .setTitle(prioritized.title ?: backup.title)
            .setDescription(prioritized.description ?: backup.description)
            .setDescriptionFormatted(prioritized.descriptionFormatted ?: backup.descriptionFormatted)
            .setPublisher(prioritized.publisher ?: backup.publisher)
            .setDistribution(prioritized.distribution ?: backup.distribution)
            .setSample(prioritized.sample ?: backup.sample)
            .setContactPoint(prioritized.contactPoint ?: backup.contactPoint)
            .setThemeUris(prioritized.themeUris ?: backup.themeUris)
            .setTheme(prioritized.theme ?: backup.theme)
            .setLosTheme(prioritized.losTheme ?: backup.losTheme)
            .setEurovocThemes(prioritized.eurovocThemes ?: backup.eurovocThemes)
            .setKeyword(prioritized.keyword ?: backup.keyword)
            .setIssued(prioritized.issued ?: backup.issued)
            .setModified(prioritized.modified ?: backup.modified)
            .setDctType(prioritized.dctType ?: backup.dctType)
            .setAccessRights(prioritized.accessRights ?: backup.accessRights)
            .setLanguage(prioritized.language ?: backup.language)
            .setPage(prioritized.page ?: backup.page)
            .setLandingPage(prioritized.landingPage ?: backup.landingPage)
            .setTemporal(prioritized.temporal ?: backup.temporal)
            .setSubject(prioritized.subject ?: backup.subject)
            .setSpatial(prioritized.spatial ?: backup.spatial)
            .setProvenance(prioritized.provenance ?: backup.provenance)
            .setAccrualPeriodicity(prioritized.accrualPeriodicity ?: backup.accrualPeriodicity)
            .setLegalBasisForAccess(prioritized.legalBasisForAccess ?: backup.legalBasisForAccess)
            .setLegalBasisForProcessing(prioritized.legalBasisForProcessing ?: backup.legalBasisForProcessing)
            .setLegalBasisForRestriction(prioritized.legalBasisForRestriction ?: backup.legalBasisForRestriction)
            .setConformsTo(prioritized.conformsTo ?: backup.conformsTo)
            .setReferences(prioritized.references ?: backup.references)
            .setHasAccuracyAnnotation(prioritized.hasAccuracyAnnotation ?: backup.hasAccuracyAnnotation)
            .setHasAvailabilityAnnotation(prioritized.hasAvailabilityAnnotation ?: backup.hasAvailabilityAnnotation)
            .setHasCompletenessAnnotation(prioritized.hasCompletenessAnnotation ?: backup.hasCompletenessAnnotation)
            .setHasCurrentnessAnnotation(prioritized.hasCurrentnessAnnotation ?: backup.hasCurrentnessAnnotation)
            .setHasRelevanceAnnotation(prioritized.hasRelevanceAnnotation ?: backup.hasRelevanceAnnotation)
            .setQualifiedAttributions(prioritized.qualifiedAttributions ?: backup.qualifiedAttributions)
            .setIsAuthoritative(prioritized.isAuthoritative)
            .setIsOpenData(prioritized.isOpenData)
            .setIsRelatedToTransportportal(prioritized.isRelatedToTransportportal)
            .setInSeries(prioritized.inSeries ?: backup.inSeries)
            .setPrev(prioritized.prev ?: backup.prev)
            .setLast(prioritized.last ?: backup.last)
            .setDatasetsInSeries(prioritized.datasetsInSeries ?: backup.datasetsInSeries)
            .setType(prioritized.type ?: backup.type)
            .setSpecializedType(prioritized.specializedType ?: backup.specializedType)
            .build()

}
