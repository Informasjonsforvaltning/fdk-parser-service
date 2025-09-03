package no.digdir.fdk.parseservice

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Shared logger instance for the FDK Parser Service.
 * 
 * This logger is configured to use the Application class as the logger name
 * and provides a centralized logging instance for the entire service.
 * 
 * The logger is configured through logback.xml and supports both console
 * and structured JSON logging formats.
 * 
 * @author FDK Team
 * @version 1.0.0
 * @since 1.0.0
 * @see Application
 */
val LOGGER: Logger = LoggerFactory.getLogger(Application::class.java)
