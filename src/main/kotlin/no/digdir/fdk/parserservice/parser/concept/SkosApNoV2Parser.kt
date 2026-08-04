package no.digdir.fdk.parserservice.parser.concept

import no.digdir.fdk.model.concept.Concept
import no.digdir.fdk.parserservice.extract.concept.extractAssociativeRelations
import no.digdir.fdk.parserservice.extract.concept.extractConceptCollection
import no.digdir.fdk.parserservice.extract.concept.extractDefinitions
import no.digdir.fdk.parserservice.extract.concept.extractGenericRelations
import no.digdir.fdk.parserservice.extract.concept.extractListOfConceptSubjects
import no.digdir.fdk.parserservice.extract.concept.extractPartitiveRelations
import no.digdir.fdk.parserservice.extract.concept.extractValueRange
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.extractListOfStrings
import no.digdir.fdk.parserservice.extract.extractLocalizedStringList
import no.digdir.fdk.parserservice.extract.extractLocalizedStrings
import no.digdir.fdk.parserservice.extract.extractOrganization
import no.digdir.fdk.parserservice.extract.extractReferenceDataCode
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.EUVOC
import no.digdir.fdk.parserservice.vocabulary.UNESKOS
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.DC_11
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Parser implementation for SKOS-AP-NO concepts.
 *
 * This parser handles the parsing of concepts according to the Norwegian
 * Application Profile for SKOS (SKOS-AP-NO) specification.
 *
 * The parser extracts concept metadata including labels, definitions,
 * relations, mappings, and other concept properties.
 *
 * @see <a href="https://data.norge.no/specification/skos-ap-no-begrep">SKOS-AP-NO Specification</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component(value = "ConceptSkosApNoV2Parser")
class SkosApNoV2Parser : BaseConceptParser() {
    override fun getDefaultLanguage(): String = LanguageCodes.NORWEGIAN.code

    override fun getVersion(): String = "2.0.15"

    override fun getSourceFormat(): String = "SKOS-AP-NO"

    private fun getAcceptableTypes(): List<Resource> = listOf(SKOS.Concept)

    override fun parse(
        model: Model,
        iri: String,
    ): Concept = parseConcept(model, iri, null)

    override fun parse(
        model: Model,
        iri: String,
        fdkId: String,
    ): Concept = parseConcept(model, iri, fdkId)

    /**
     * Parses an RDF model into a Concept object according to SKOS-AP-NO.
     *
     * This method extracts the FDK record when fdkId is present
     * and builds a complete Concept object with all available metadata.
     *
     * @param model The Jena RDF model containing the concept
     * @param iri The IRI of the concept
     * @param fdkId The FDK ID of the concept
     * @return The parsed Concept object
     * @throws IllegalArgumentException if the model is null or invalid
     * @throws UnsupportedOperationException if no valid FDK record is found
     */
    private fun parseConcept(
        model: Model,
        iri: String,
        fdkId: String?,
    ): Concept {
        val acceptableTypes = getAcceptableTypes()
        if (acceptableTypes.none { model.containsTriple(iri, RDF.type.uri, URI.create(it.uri)) }) {
            throw NoAcceptableTypesException("No acceptable types found for $iri")
        }

        val conceptResource = resourceOfIRI(model, iri)

        val builder = Concept.newBuilder()

        if (fdkId != null) {
            val recordResource = fdkRecord(conceptResource, fdkId)
            builder.addFdkData(recordResource)
        }

        builder.addCommonConceptValues(conceptResource)

        builder.setCollection(conceptResource.extractConceptCollection())

        builder.setPrefLabel(conceptResource.extractLocalizedStrings(SKOS.prefLabel))
        builder.setAltLabel(conceptResource.extractLocalizedStringList(SKOS.altLabel))
        builder.setHiddenLabel(conceptResource.extractLocalizedStringList(SKOS.hiddenLabel))

        val definitions = conceptResource.extractDefinitions()
        builder.setDefinitions(definitions)
        builder.setDefinition(
            definitions
                ?.firstOrNull { it.targetGroup == null }
                ?: definitions?.firstOrNull(),
        )

        builder.setStatus(
            conceptResource
                .extractReferenceDataCode(
                    EUVOC.status,
                    DC_11.identifier,
                    SKOS.prefLabel,
                )?.prefLabel,
        )

        builder.setSubject(conceptResource.extractListOfConceptSubjects())
        builder.setExample(conceptResource.extractLocalizedStrings(SKOS.example))
        builder.setRemark(conceptResource.extractLocalizedStrings(SKOS.scopeNote))
        builder.setRange(conceptResource.extractValueRange())

        builder.setCreated(conceptResource.extractStringValue(DCTerms.created))
        builder.setCreator(conceptResource.extractOrganization(DCTerms.creator))

        builder.setValidFromIncluding(conceptResource.extractStringValue(EUVOC.startDate))
        builder.setValidToIncluding(conceptResource.extractStringValue(EUVOC.endDate))

        builder.setExactMatch(conceptResource.extractListOfStrings(SKOS.exactMatch))
        builder.setCloseMatch(conceptResource.extractListOfStrings(SKOS.closeMatch))
        builder.setMemberOf(conceptResource.extractListOfStrings(UNESKOS.memberOf))

        builder.setAssociativeRelation(conceptResource.extractAssociativeRelations())
        builder.setPartitiveRelation(conceptResource.extractPartitiveRelations())
        builder.setGenericRelation(conceptResource.extractGenericRelations())

        return builder.build()
    }
}
