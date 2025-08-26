package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.model.LocalizedStrings
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource

fun Resource.extractLocalizedStrings(pred: Property): LocalizedStrings? {
    val values = listProperties(pred)
        .asSequence()
        .mapNotNull { it.extractStringLanguagePair() }
        .toMap()

    val builder = LocalizedStrings.newBuilder()

    return if (values.isEmpty()) null
    else builder.setNb(values["nb"] ?: values[""])
        .setNn(values["nn"])
        .setNo(values["no"])
        .setEn(values["en"])
        .build()
}

fun LocalizedStrings.descriptionHtmlCleaner(): LocalizedStrings {
    val builder = LocalizedStrings.newBuilder()
    val regex = Regex("<.*?>|&([a-z0-9]+|#[0-9]{1,6}|#x[0-9a-f]{1,6});")

    return builder
        .setNo(no?.replace(regex, ""))
        .setNb(nb?.replace(regex, ""))
        .setNn(nn?.replace(regex, ""))
        .setEn(en?.replace(regex, ""))
        .build()
}
