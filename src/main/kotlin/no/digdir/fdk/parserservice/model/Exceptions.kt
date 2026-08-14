package no.digdir.fdk.parserservice.model

open class RecoverableParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

open class UnrecoverableParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NoAcceptableFDKRecordsException(message: String) : RecoverableParseException(message)

class MultipleFDKRecordsException(message: String) : RecoverableParseException(message)

class NoAcceptableTypesException(message: String) : RecoverableParseException(message)

class NoResourceFoundException(message: String) : RecoverableParseException(message)

class UnableToParseException(message: String) : UnrecoverableParseException(message)

class NoParserMatchedException(message: String, cause: Throwable? = null) : UnrecoverableParseException(message, cause)
