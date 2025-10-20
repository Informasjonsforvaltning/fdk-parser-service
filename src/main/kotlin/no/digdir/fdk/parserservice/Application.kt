package no.digdir.fdk.parserservice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

/**
 * Main application class for the FDK Parser Service.
 * 
 * This service is responsible for parsing RDF data from reasoned events (Kafka) 
 * and converting them to JSON format using Apache Jena and Avro schemas.
 * 
 * The service supports parsing of DCAT-AP-NO datasets and other RDF resources
 * according to the Norwegian Data Catalog specifications.
 * 
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
open class Application

/**
 * Application entry point.
 * 
 * Starts the Spring Boot application with the provided command line arguments.
 * 
 * @param args Command line arguments passed to the application
 */
fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
