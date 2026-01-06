package no.digdir.fdk.parserservice.parser.event

import no.digdir.fdk.model.event.Event
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.CV
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Parser implementation for CPSVNO events.
 *
 * This parser handles the parsing of events according to the Norwegian
 * Core Public Service Vocabulary (CPSVNO) specification.
 *
 * The parser extracts event metadata including title, description, type,
 * relation, mayInitiate, subject, and distribution.
 *
 * @see <a href="https://data.norge.no/specification/cpsv-ap-no">CPSV-AP-NO Specification</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component(value = "EventCpsvNoV0Parser")
class CpsvApNoV0Parser : BaseEventParser() {
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
    override fun getVersion(): String = "0.9"

    /**
     * Gets the source format identifier.
     *
     * @return "CPSV-AP-NO"
     */
    override fun getSourceFormat(): String = "CPSV-AP-NO"

    /**
     * Gets the acceptable RDF types for events.
     *
     * @return List containing CV.Event, CV.BusinessEvent, and CV.LifeEvent
     */
    override fun getAcceptableTypes(): List<Resource> =
        listOf(
            ResourceFactory.createResource(CV.Event.uri),
            ResourceFactory.createResource(CV.BusinessEvent.uri),
            ResourceFactory.createResource(CV.LifeEvent.uri),
        )

    override fun parse(
        model: Model,
        iri: String,
    ): Event = parseEvent(model, iri, null)

    override fun parse(
        model: Model,
        iri: String,
        fdkId: String,
    ): Event = parseEvent(model, iri, fdkId)

    /**
     * Parses an RDF model into an Event object according to CPSVNO.
     *
     * This method extracts the FDK record when fdkId is present
     * and builds a complete Event object with all available metadata.
     *
     * @param model The Jena RDF model containing the event
     * @param iri The IRI of the event
     * @param fdkId The FDK ID of the event
     * @return The parsed Event object
     * @throws IllegalArgumentException if the model is null or invalid
     * @throws UnsupportedOperationException if no valid FDK record is found
     */
    private fun parseEvent(
        model: Model,
        iri: String,
        fdkId: String?,
    ): Event {
        val acceptableTypes = getAcceptableTypes()
        if (acceptableTypes.none { model.containsTriple(iri, RDF.type.uri, URI.create(it.uri)) }) {
            throw NoAcceptableTypesException("No acceptable types found for $iri")
        }

        val eventResource = resourceOfIRI(model, iri)

        val builder = Event.newBuilder()

        if (fdkId != null) {
            val recordResource = fdkRecord(eventResource, fdkId)
            builder.addFdkData(recordResource)
        }

        builder.addCommonEventValues(eventResource)

        return builder.build()
    }
}
