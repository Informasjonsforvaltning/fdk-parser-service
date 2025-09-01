package no.digdir.fdk.parseservice.parser

import no.digdir.fdk.model.dataset.Dataset
import org.apache.jena.rdf.model.Model

interface RdfParserStrategy<T> {
    fun parse(model: Model): T
}

typealias DatasetParserStrategy = RdfParserStrategy<Dataset>
