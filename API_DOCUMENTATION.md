# FDK Parser Service - API Documentation

## Overview

The FDK Parser Service is responsible for parsing RDF data from reasoned events (Kafka) and converting them to JSON format using Apache Jena and Avro schemas. The service supports parsing of DCAT-AP-NO datasets and other RDF resources according to the Norwegian Data Catalog specifications.

## Architecture

The service follows a clean architecture pattern with clear separation of concerns:

- **Parser Layer**: Strategy pattern for different RDF parsing implementations
- **Extraction Layer**: Utility functions for extracting data from RDF resources
- **Vocabulary Layer**: Constants and properties for various RDF vocabularies
- **Utility Layer**: Helper functions for data conversion and processing

## Core Components

### 1. Parser Strategy

#### `RdfParserStrategy<T>`

Generic interface for RDF parsing strategies.

```kotlin
interface RdfParserStrategy<T> {
    fun parse(model: Model): T
}
```

#### `DatasetParserStrategy`

Type alias for dataset-specific parsing strategies.

```kotlin
typealias DatasetParserStrategy = RdfParserStrategy<Dataset>
```

### 2. Base Dataset Parser

#### `BaseDatasetParser`

Abstract base class providing common functionality for DCAT-AP dataset parsers.

**Key Methods:**

- `parse(model: Model): Dataset` - Abstract method for parsing RDF models
- `addCommonDatasetValues(datasetResource: Resource)` - Adds common dataset properties

**Abstract Methods:**

- `getDefaultLanguage(): String` - Returns default language for the parser
- `getVersion(): String` - Returns parser version
- `getSourceFormat(): String` - Returns source format identifier
- `getFDKURIPattern(): String` - Returns FDK URI pattern
- `getAcceptableTypes(): List<Resource>` - Returns acceptable RDF types

### 3. DCAT-AP-NO v1.1 Parser

#### `DcatApNoV1Parser`

Concrete implementation for parsing DCAT-AP-NO version 1.1 datasets.

**Usage Example:**

```kotlin
val parser = DcatApNoV1Parser()
val model = ModelFactory.createDefaultModel()
model.read(inputStream, null, "TURTLE")

val dataset = parser.parse(model)
println("Dataset title: ${dataset.title?.no}")
println("Dataset URI: ${dataset.uri}")
```

**Supported Properties:**

- Basic metadata (title, description, identifier)
- Publisher and organization information
- Themes and classifications (EU Data Themes, LOS, EuroVoc)
- Temporal information (issued, modified dates)
- Distributions and samples
- Keywords and access rights
- Spatial and provenance information

## Extraction Utilities

### Generic RDF Functions

#### `Resource.singleObjectStatement(pred: Property): Statement?`

Gets a single object statement for a given predicate.

```kotlin
val statement = resource.singleObjectStatement(DCTerms.title)
```

#### `Resource.singleResource(pred: Property): Resource?`

Gets a single resource for a given predicate.

```kotlin
val publisher = resource.singleResource(DCTerms.publisher)
```

#### `Resource.listResources(pred: Property): List<Resource>?`

Gets a list of resources for a given predicate.

```kotlin
val themes = resource.listResources(DCAT.theme)
```

#### `Resource.extractStringValue(pred: Property): String?`

Extracts a single string value for a given predicate.

```kotlin
val identifier = resource.extractStringValue(DCTerms.identifier)
```

#### `Resource.extractListOfStrings(pred: Property): List<String>?`

Extracts a list of string values for a given predicate.

```kotlin
val keywords = resource.extractListOfStrings(DCAT.keyword)
```

### Localized Strings

#### `Resource.extractLocalizedStrings(pred: Property): LocalizedStrings?`

Extracts localized strings with language tags.

```kotlin
val resource = model.getResource("http://example.org/dataset")
val title = resource.extractLocalizedStrings(DCTerms.title)

// Access localized values
println("Norwegian title: ${title?.no}")
println("English title: ${title?.en}")
```

#### `LocalizedStrings.descriptionHtmlCleaner(): LocalizedStrings`

Cleans HTML tags from localized strings.

```kotlin
val description = LocalizedStrings().apply {
    no = "<div>Dette er en <strong>beskrivelse</strong></div>"
    en = "<p>This is a <em>description</em></p>"
}

val cleanDescription = description.descriptionHtmlCleaner()
// cleanDescription.no = "Dette er en beskrivelse"
// cleanDescription.en = "This is a description"
```

### Organization Extraction

#### `Resource.extractOrganization(pred: Property): Organization?`

Extracts organization information from an RDF resource.

```kotlin
val publisher = resource.extractOrganization(DCTerms.publisher)
println("Organization name: ${publisher?.name}")
println("Organization URI: ${publisher?.uri}")
```

## FDK Harvest Data

### `fdkRecord(model: Model, acceptableTypes: List<Resource>, fdkURIPattern: String): Resource`

Extracts a valid FDK record from an RDF model.

```kotlin
val record = fdkRecord(model, listOf(DCAT.Dataset), "fellesdatakatalog.digdir.no")
```

### `primaryTopicFromFdkRecord(recordResource: Resource, acceptableTypes: List<Resource>): Resource`

Extracts the primary topic (dataset) from an FDK record.

```kotlin
val dataset = primaryTopicFromFdkRecord(record, listOf(DCAT.Dataset))
```

### `harvestMetaData(recordResource: Resource): HarvestMetaData?`

Extracts harvest metadata from an FDK record.

```kotlin
val harvestData = harvestMetaData(record)
println("Modified: ${harvestData?.modified}")
println("First harvested: ${harvestData?.firstHarvested}")
```

## Vocabulary Classes

### FDK Vocabulary

```kotlin
FDK.isAuthoritative          // Property for authoritative datasets
FDK.isRelatedToTransportportal // Property for transport portal datasets
FDK.isOpenData              // Property for open data datasets
FDK.themePath               // Property for theme path information
```

### ADMS Vocabulary

```kotlin
ADMS.identifier             // Property for asset identifiers
ADMS.sample                 // Property for asset samples
```

### EU Authority Vocabulary

```kotlin
EUAT.authorityCode          // Property for authority codes
```

### Schema.org Vocabulary

```kotlin
SCHEMA.startDate            // Property for start dates
SCHEMA.endDate              // Property for end dates
```

## Utility Functions

### Avro to JSON Conversion

#### `avroToJson(avroObject: T, schema: Schema): String`

Converts an Avro object to JSON string using a custom flattening encoder.

```kotlin
val dataset = Dataset().apply {
    uri = "http://example.org/dataset"
    title = LocalizedStrings().apply { no = "Test Dataset" }
}

val schema = Dataset.getClassSchema()
val jsonString = avroToJson(dataset, schema)
println(jsonString) // {"uri": "http://example.org/dataset", "title": {"no": "Test Dataset"}}
```

### FlatteningJsonEncoder

Custom Avro JSON encoder that flattens union types for better nullable handling.

**Key Features:**

- Removes union type wrappers
- Handles nullable fields correctly
- Produces cleaner JSON output

## Error Handling

The service implements comprehensive error handling:

- **Graceful Degradation**: Functions return null instead of throwing exceptions when possible
- **Logging**: Warnings are logged for unexpected data or extraction failures
- **Validation**: Input validation for RDF models and resources
- **Custom Exceptions**: Specific exception types for different error scenarios

## Configuration

The service supports multiple configuration profiles:

- **develop**: Development environment
- **test**: Testing environment
- **staging**: Staging environment
- **prod**: Production environment

## Logging

The service uses structured JSON logging with the following features:

- **Log Levels**: Configurable per package
- **Structured Output**: JSON format for better parsing
- **Service Context**: Includes service name in log entries
- **Async Logging**: Non-blocking log appenders

## Testing

The service includes comprehensive unit tests covering:

- Dataset extraction scenarios
- Localized string handling
- HTML cleaning functionality
- Organization extraction
- Harvest data processing
- Error handling cases

## Dependencies

- **Java 21**: Runtime environment
- **Kotlin 2.2.10**: Programming language
- **Spring Boot 3.5.5**: Application framework
- **Apache Jena 5.5.0**: RDF processing
- **Apache Avro 1.12.0**: Data serialization
- **SLF4J + Logback**: Logging framework

## Performance Considerations

- **Query Caching**: Frequently used SPARQL queries are cached
- **Resource Management**: Proper cleanup of RDF models
- **Async Processing**: Non-blocking operations where possible
- **Memory Optimization**: Efficient data structures and processing

## Security

- **Input Validation**: All RDF input is validated
- **Resource Limits**: Memory and processing limits enforced
- **Error Sanitization**: Sensitive information is not exposed in error messages

## Monitoring

The service includes monitoring capabilities:

- **Health Checks**: `/ping` and `/ready` endpoints
- **Metrics**: Prometheus-compatible metrics
- **Logging**: Structured logging for analysis
- **Tracing**: Request tracing support

## Future Enhancements

Planned improvements include:

- Additional DCAT-AP versions support
- Enhanced error handling with custom exceptions
- Performance optimizations
- Extended vocabulary support
- Improved documentation and examples
