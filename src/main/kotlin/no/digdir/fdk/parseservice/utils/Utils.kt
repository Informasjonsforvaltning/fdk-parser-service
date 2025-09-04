package no.digdir.fdk.parseservice.utils

import org.apache.avro.Schema
import org.apache.avro.io.DatumWriter
import org.apache.avro.io.Encoder
import org.apache.avro.specific.SpecificDatumWriter
import java.io.ByteArrayOutputStream

/**
 * Utility functions for data conversion and processing.
 * 
 * This object contains helper functions for converting between different
 * data formats, particularly Avro to JSON conversion.
 * 
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
object Utils {
    // Utility functions can be added here in the future
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
inline fun <reified T> avroToJson(avroObject: T, schema: Schema): String {
    val outputStream = ByteArrayOutputStream()
    val datumWriter: DatumWriter<T> = SpecificDatumWriter(schema)
    val encoder: Encoder = FlatteningJsonEncoder(schema, outputStream)
    datumWriter.write(avroObject, encoder)
    encoder.flush()
    return String(outputStream.toByteArray())
}
