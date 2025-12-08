package no.digdir.fdk.parserservice.parser.service

import no.digdir.fdk.model.service.Service
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.extractListOfUriWithLabel
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.extract.service.extractListOfServiceChannelsV1
import no.digdir.fdk.parserservice.extract.service.extractListOfServiceCostsV1
import no.digdir.fdk.parserservice.extract.service.extractListOfServiceEvidence
import no.digdir.fdk.parserservice.extract.service.extractListOfServiceOutput
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.CPSV
import no.digdir.fdk.parserservice.vocabulary.CPSVNO
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Parser implementation for CPSVNO services.
 *
 * This parser handles the parsing of services according to the Norwegian
 * Core Public Service Vocabulary (CPSVNO) specification.
 *
 * The parser extracts service metadata including contact points, channels,
 * costs, requirements, outputs, and other properties defined in the CPSVNO specification.
 *
 * @see <a href="https://data.norge.no/specification/cpsv-ap-no">CPSV-AP-NO Specification</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component(value = "ServiceCpsvNoV1Parser")
class CpsvApNoV1Parser : BaseServiceParser() {
    /**
     * Gets the default language for CPSVNO.
     *
     * @return "no" (Norwegian)
     */
    override fun getDefaultLanguage(): String = LanguageCodes.NORWEGIAN.code

    /**
     * Gets the version string for this parser.
     *
     * @return "0.9"
     */
    override fun getVersion(): String = "1.1.2"

    /**
     * Gets the source format identifier.
     *
     * @return "CPSV-AP-NO"
     */
    override fun getSourceFormat(): String = "CPSV-AP-NO"

    /**
     * Gets the acceptable RDF types for services.
     *
     * @return List containing CPSVNO.Service and CPSV.PublicService
     */
    override fun getAcceptableTypes(): List<Resource> =
        listOf(
            ResourceFactory
                .createResource(CPSVNO.Service.uri),
            ResourceFactory
                .createResource(CPSV.PublicService.uri),
        )

    override fun parse(
        model: Model,
        iri: String,
    ): Service = parseService(model, iri, null)

    override fun parse(
        model: Model,
        iri: String,
        fdkId: String,
    ): Service = parseService(model, iri, fdkId)

    /**
     * Parses an RDF model into a Service object according to CPSVNO.
     *
     * This method extracts the FDK record when fdkId is present
     * and builds a complete Service object with all available metadata.
     *
     * @param model The Jena RDF model containing the service
     * @param iri The IRI of the service
     * @param fdkId The FDK ID of the service
     * @return The parsed Service object
     * @throws IllegalArgumentException if the model is null or invalid
     * @throws UnsupportedOperationException if no valid FDK record is found
     */
    private fun parseService(
        model: Model,
        iri: String,
        fdkId: String?,
    ): Service {
        val acceptableTypes = getAcceptableTypes()
        if (acceptableTypes.none { model.containsTriple(iri, RDF.type.uri, URI.create(it.uri)) }) {
            throw NoAcceptableTypesException("No acceptable types found for $iri")
        }

        val serviceResource = resourceOfIRI(model, iri)

        val builder = Service.newBuilder()

        if (fdkId != null) {
            val recordResource = fdkRecord(serviceResource, fdkId)
            builder.addFdkData(recordResource)
        }

        builder.addCommonServiceValues(serviceResource)

        builder.setRelation(serviceResource.extractListOfUriWithLabel(CV.relatedService, DCTerms.title))
        builder.setHasChannel(serviceResource.extractListOfServiceChannelsV1())
        builder.setHasInput(serviceResource.extractListOfServiceEvidence(CPSVNO.hasRequiredEvidence))
        builder.setProduces(serviceResource.extractListOfServiceOutput(CPSVNO.hasPossibleOutput))
        builder.setHasCost(serviceResource.extractListOfServiceCostsV1())

        return builder.build()
    }
}
