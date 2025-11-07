package no.digdir.fdk.parserservice.utils

import org.apache.avro.Schema
import org.apache.avro.io.DatumWriter
import org.apache.avro.io.Encoder
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.Lang
import tools.jackson.databind.JsonNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.io.ByteArrayOutputStream
import java.io.StringReader

/**
 * Reads a graph in Turtle format into the Jena Model.
 *
 * This extension function parses a Turtle-formatted RDF graph string
 * and loads it into the current Jena Model instance. It handles the
 * conversion from Turtle syntax to the internal Jena representation.
 *
 * @param turtleGraph The Turtle-formatted RDF graph as a string
 * @see Model.read
 */
fun Model.readTurtle(turtleGraph: String) {
    val reader = StringReader(turtleGraph)
    read(reader, "", Lang.TURTLE.name)
}

/**
 * Converts an Avro object to JSON string using a custom flattening encoder.
 *
 * This function serializes an Avro object to JSON format using the provided schema.
 * It uses a custom FlatteningJsonEncoder that handles nullable unions correctly
 * by flattening the union structure.
 *
 * ## Usage Example
 *
 * ```kotlin
 * val dataset = Dataset().apply {
 *     uri = "http://example.org/dataset"
 *     title = LocalizedStrings().apply { no = "Test Dataset" }
 * }
 *
 * val schema = Dataset.getClassSchema()
 * val jsonString = avroToJson(dataset, schema)
 * println(jsonString) // {"uri": "http://example.org/dataset", "title": {"no": "Test Dataset"}}
 * ```
 *
 * @param T The type of the Avro object
 * @param avroObject The Avro object to convert
 * @param schema The Avro schema for the object
 * @return JSON string representation of the Avro object
 * @throws Exception if serialization fails
 * @see FlatteningJsonEncoder
 */
inline fun <reified T> avroToJson(
    avroObject: T,
    schema: Schema,
): JsonNode {
    val mapper = jacksonObjectMapper()
    val outputStream = ByteArrayOutputStream()
    val datumWriter: DatumWriter<T> = SpecificDatumWriter(schema)
    val encoder: Encoder = FlatteningJsonEncoder(schema, outputStream)
    datumWriter.write(avroObject, encoder)
    encoder.flush()
    return mapper.readTree(outputStream.toByteArray())
}
