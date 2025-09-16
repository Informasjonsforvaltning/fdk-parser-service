package no.digdir.fdk.parseservice.extract

import no.digdir.fdk.parseservice.LOGGER
import no.digdir.fdk.parseservice.model.LanguageCodes
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.Statement
import java.net.URI

/**
 * Extension function to get a single object statement for a given predicate.
 * 
 * This function retrieves the first statement with the given predicate from the resource.
 * If multiple statements exist, a warning is logged and the first one is returned.
 * 
 * @param pred The property predicate to search for
 * @return The first statement with the given predicate, or null if none exists
 * @see Resource.listProperties
 */
fun Resource.singleObjectStatement(pred: Property): Statement? {
    val properties = listProperties(pred).toList()

    if (properties.size > 1) LOGGER.warn("Multiple objects of ${pred.uri} found on $uri when only expecting 1, selecting random as backup")

    return if (properties.isEmpty()) null else properties.first()
}

/**
 * Extension function to get a single resource for a given predicate.
 * 
 * This function extracts a single resource from the statements with the given predicate.
 * If the statement object is not a resource, null is returned.
 * 
 * @param pred The property predicate to search for
 * @return The resource object, or null if not found or not a resource
 * @see Resource.singleObjectStatement
 */
fun Resource.singleResource(pred: Property): Resource? =
    try{
        singleObjectStatement(pred)?.resource?.takeIf { it.isResource }
    } catch (ex: Exception) {
        LOGGER.warn("Failed to extract ${pred.uri}, found on $uri, as resource", ex)
        null
    }

/**
 * Extension function to get a list of resources for a given predicate.
 * 
 * This function extracts all resources from the statements with the given predicate.
 * Only statements with resource objects are included in the result.
 * 
 * @param pred The property predicate to search for
 * @return List of resource objects, or null if extraction fails
 * @see Resource.listProperties
 */
fun Resource.listResources(pred: Property): List<Resource>? =
    try{
        listProperties(pred).asSequence().map { it.resource }.filter { it.isResource }.toList()
    } catch (ex: Exception) {
        LOGGER.warn("Failed to extract ${pred.uri}, found on $uri, as resource", ex)
        null
    }

/**
 * Private extension function to extract string value from a statement.
 * 
 * This function handles both URI resources and literal values, returning
 * the URI string for resources and the literal string for other values.
 * 
 * @return The string value, or null if extraction fails
 */
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

/**
 * Extension function to extract a single string value for a given predicate.
 * 
 * @param pred The property predicate to search for
 * @return The string value, or null if not found
 * @see Resource.singleObjectStatement
 */
fun Resource.extractStringValue(pred: Property): String? =
    singleObjectStatement(pred)?.extractStringValue()

/**
 * Extension function to extract a list of string values for a given predicate.
 * 
 * This function extracts all string values from statements with the given predicate.
 * 
 * @param pred The property predicate to search for
 * @return List of string values, or null if no values found
 * @see Resource.listProperties
 */
fun Resource.extractListOfStrings(pred: Property): List<String>? =
    listProperties(pred).asSequence()
        .mapNotNull { it.extractStringValue() }
        .toList()
        .ifEmpty { null }

/**
 * Extension function to extract a string value with its language tag.
 * 
 * This function extracts both the string value and language tag from a statement.
 * 
 * @return A pair of (language, string) or null if extraction fails
 */
fun Statement.extractStringLanguagePair(): Pair<LanguageCodes, String>? {
    try {
        val lang = when(language) {
            null -> LanguageCodes.NONE
            "" -> LanguageCodes.NONE
            "no" -> LanguageCodes.NORWEGIAN
            "nb" -> LanguageCodes.NORWEGIAN_BOKMAL
            "nn" -> LanguageCodes.NORWEGIAN_NYNORSK
            "en" -> LanguageCodes.ENGLISH
            else -> null
        }
        val str = string
        return when {
            str == null -> null
            lang == null -> null
            else -> Pair(lang, str)
        }
    } catch (ex: Exception) {
        LOGGER.warn("Failed to get string language pair from ${toString()}", ex)
        return null
    }
}

/**
 * Checks if a model contains a specific triple with string object.
 * 
 * This function uses SPARQL ASK queries to check for the existence of a triple.
 * 
 * @param subj The subject URI
 * @param pred The predicate URI
 * @param obj The URI object value
 * @return true if the triple exists, false otherwise
 */
fun Model.containsTriple(subj: String, pred: String, obj: URI): Boolean {
    val askQuery =  "ASK { <$subj> <$pred> <$obj> }"

    return try {
        val query = QueryFactory.create(askQuery)
        QueryExecutionFactory.create(query, this).execAsk()
    } catch (ex: Exception) {
        false
    }
}

/**
 * Checks if a model contains a specific triple with string object.
 *
 * This function uses SPARQL ASK queries to check for the existence of a triple.
 *
 * @param subj The subject URI
 * @param pred The predicate URI
 * @param obj The string object value
 * @return true if the triple exists, false otherwise
 */
fun Model.containsTriple(subj: String, pred: String, obj: String): Boolean {
    val askQuery = "ASK { <$subj> <$pred> '$obj' }"

    return try {
        val query = QueryFactory.create(askQuery)
        QueryExecutionFactory.create(query, this).execAsk()
    } catch (ex: Exception) {
        false
    }
}

/**
 * Checks if a model contains a specific triple with boolean object.
 * 
 * @param subj The subject URI
 * @param pred The predicate URI
 * @param obj The boolean object value
 * @return true if the triple exists, false otherwise
 */
fun Model.containsTriple(subj: String, pred: String, obj: Boolean): Boolean {
    val askQuery = "ASK { <$subj> <$pred> $obj }"

    return try {
        val query = QueryFactory.create(askQuery)
        QueryExecutionFactory.create(query, this).execAsk()
    } catch (ex: Exception) {
        false
    }
}

/**
 * Checks if a statement's object is a URI resource.
 * 
 * @param stmt The statement to check
 * @return true if the object is a URI resource, false otherwise
 */
fun isURIResource(stmt: Statement): Boolean = try {
    stmt.resource?.isURIResource == true
} catch (ex: Exception) {
    false
}

/**
 * Checks if a statement's object is a resource (URI or blank node).
 * 
 * @param stmt The statement to check
 * @return true if the object is a resource, false otherwise
 */
fun isResource(stmt: Statement): Boolean = try {
    stmt.resource?.isResource == true
} catch (ex: Exception) {
    false
}
