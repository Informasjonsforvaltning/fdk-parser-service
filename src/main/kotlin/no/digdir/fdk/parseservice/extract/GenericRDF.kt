package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.parseservice.LOGGER
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement

fun Resource.singleObjectStatement(pred: Property): Statement? {
    val properties = listProperties(pred).toList()

    if (properties.size > 1) LOGGER.warn("Multiple objects of ${pred.uri} found on $uri when only expecting 1, selecting random as backup")

    return if (properties.isEmpty()) null else properties.first()
}

fun Resource.singleResource(pred: Property): Resource? =
    try{
        singleObjectStatement(pred)?.resource?.takeIf { it.isResource }
    } catch (ex: Exception) {
        LOGGER.warn("Failed to extract ${pred.uri}, found on $uri, as resource", ex)
        null
    }

fun Resource.listResources(pred: Property): List<Resource>? =
    try{
        listProperties(pred).asSequence().map { it.resource }.filter { it.isResource }.toList()
    } catch (ex: Exception) {
        LOGGER.warn("Failed to extract ${pred.uri}, found on $uri, as resource", ex)
        null
    }

private fun Statement.extractStringValue(): String? {
    try {
        return when {
            isURIResource(this) -> resource?.uri
            else -> string
        }
    } catch (ex: Exception) {
        LOGGER.warn("Failed to get string value from ${toString()}", ex)
        return null
    }
}

fun Resource.extractStringValue(pred: Property): String? =
    singleObjectStatement(pred)?.extractStringValue()

fun Resource.extractListOfStrings(pred: Property): List<String>? =
    listProperties(pred).asSequence()
        .mapNotNull { it.extractStringValue() }
        .toList()
        .ifEmpty { null }

fun Statement.extractStringLanguagePair(): Pair<String, String>? {
    try {
        val lang = language ?: ""
        val str = string
        return when {
            str == null -> null
            else -> Pair(lang, str)
        }
    } catch (ex: Exception) {
        LOGGER.warn("Failed to get string language pair from ${toString()}", ex)
        return null
    }
}

fun Model.containsTriple(subj: String, pred: String, obj: String, objectIsURI: Boolean): Boolean {
    val askQuery = if (objectIsURI) "ASK { <$subj> <$pred> <$obj> }" else "ASK { <$subj> <$pred> $obj }"

    return try {
        val query = QueryFactory.create(askQuery)
        QueryExecutionFactory.create(query, this).execAsk()
    } catch (ex: Exception) {
        false
    }
}

fun Model.containsTriple(subj: String, pred: String, obj: Boolean): Boolean {
    val askQuery = "ASK { <$subj> <$pred> $obj }"

    return try {
        val query = QueryFactory.create(askQuery)
        QueryExecutionFactory.create(query, this).execAsk()
    } catch (ex: Exception) {
        false
    }
}

fun isURIResource(stmt: Statement): Boolean = try {
    stmt.resource?.isURIResource == true
} catch (ex: Exception) {
    false
}

fun isResource(stmt: Statement): Boolean = try {
    stmt.resource?.isResource == true
} catch (ex: Exception) {
    false
}
