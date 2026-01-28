package no.digdir.fdk.parserservice.parser.concept

import no.digdir.fdk.model.concept.Concept
import no.digdir.fdk.parserservice.extract.concept.extractAssociativeRelationsV1
import no.digdir.fdk.parserservice.extract.concept.extractConceptCollectionV1
import no.digdir.fdk.parserservice.extract.concept.extractDefinitionsV1
import no.digdir.fdk.parserservice.extract.concept.extractGenericRelationsV1
import no.digdir.fdk.parserservice.extract.concept.extractLabelsV1
import no.digdir.fdk.parserservice.extract.concept.extractListOfConceptSubjectsV1
import no.digdir.fdk.parserservice.extract.concept.extractPartitiveRelationsV1
import no.digdir.fdk.parserservice.extract.concept.extractPrefLabelV1
import no.digdir.fdk.parserservice.extract.containsTriple
import no.digdir.fdk.parserservice.extract.extractStringValue
import no.digdir.fdk.parserservice.extract.fdk.addFdkData
import no.digdir.fdk.parserservice.extract.fdk.fdkRecord
import no.digdir.fdk.parserservice.extract.fdk.resourceOfIRI
import no.digdir.fdk.parserservice.extract.singleResource
import no.digdir.fdk.parserservice.model.LanguageCodes
import no.digdir.fdk.parserservice.model.NoAcceptableTypesException
import no.digdir.fdk.parserservice.vocabulary.SCHEMA
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.SKOS
import org.apache.jena.vocabulary.SKOSXL
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Parser implementation for SKOS-AP-NO concepts.
 *
 * This parser handles the parsing of concepts according to the deprecated
 * version 1.1.1 of the Norwegian Application Profile for SKOS (SKOS-AP-NO) specification.
 *
 * The parser extracts concept metadata including labels, definitions,
 * relations, mappings, and other concept properties.
 *
 * @see <a href="https://data.norge.no/specification/skos-ap-no-begrep">SKOS-AP-NO Specification</a>
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component(value = "ConceptSkosApNoV1Parser")
class SkosApNoV1Parser : BaseConceptParser() {
    /**
     * Gets the default language for SKOS-AP-NO.
     *
     * @return "no" (Norwegian)
     */
    override fun getDefaultLanguage(): String = LanguageCodes.NORWEGIAN.code

    /**
     * Gets the version string for this parser.
     *
     * @return "1.0"
     */
    override fun getVersion(): String = "1.1.1"

    /**
     * Gets the source format identifier.
     *
     * @return "SKOS-AP-NO"
     */
    override fun getSourceFormat(): String = "SKOS-AP-NO"

    /**
     * Gets the acceptable RDF types for concepts.
     *
     * @return List containing SKOS.Concept
     */
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
     * Parses an RDF model into a Concept object according to SKOS-AP-NO v1.1.1.
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

        builder.setCollection(conceptResource.extractConceptCollectionV1())

        builder.setPrefLabel(conceptResource.extractPrefLabelV1())
        builder.setAltLabel(conceptResource.extractLabelsV1(SKOSXL.altLabel))
        builder.setHiddenLabel(conceptResource.extractLabelsV1(SKOSXL.hiddenLabel))

        val definitions = conceptResource.extractDefinitionsV1()
        builder.setDefinitions(definitions)
        builder.setDefinition(
            definitions
                ?.firstOrNull { it.targetGroup == null }
                ?: definitions?.firstOrNull(),
        )

        builder.setSubject(conceptResource.extractListOfConceptSubjectsV1())

        val temporalResource = conceptResource.singleResource(DCTerms.temporal)
        builder.setValidFromIncluding(temporalResource?.extractStringValue(SCHEMA.startDate))
        builder.setValidToIncluding(temporalResource?.extractStringValue(SCHEMA.endDate))

        builder.setAssociativeRelation(conceptResource.extractAssociativeRelationsV1())
        builder.setPartitiveRelation(conceptResource.extractPartitiveRelationsV1())
        builder.setGenericRelation(conceptResource.extractGenericRelationsV1())

        // The following properties are not implemented in SKOS-AP-NO v1.1.1
        builder.setCloseMatch(null)
        builder.setCreated(null)
        builder.setCreator(null)
        builder.setExactMatch(null)
        builder.setExample(null)
        builder.setMemberOf(null)
        builder.setRange(null)
        builder.setRemark(null)
        builder.setStatus(null)

        return builder.build()
    }
}
