package no.digdir.fdk.parseservice.utils

import org.apache.avro.Schema
import org.apache.avro.io.DatumWriter
import org.apache.avro.io.Encoder
import org.apache.avro.specific.SpecificDatumWriter
import java.io.ByteArrayOutputStream

inline fun <reified T> avroToJson(avroObject: T, schema: Schema): String {
    val outputStream = ByteArrayOutputStream()
    val datumWriter: DatumWriter<T> = SpecificDatumWriter(schema)
    val encoder: Encoder = FlatteningJsonEncoder(schema, outputStream)
    datumWriter.write(avroObject, encoder)
    encoder.flush()
    return String(outputStream.toByteArray())
}
