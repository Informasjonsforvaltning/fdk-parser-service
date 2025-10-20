package no.digdir.fdk.parserservice.model

open class RecoverableParseException(message: String) : Exception(message)

open class UnrecoverableParseException(message: String) : Exception(message)

class NoAcceptableFDKRecordsException(message: String) : RecoverableParseException(message)

class MultipleFDKRecordsException(message: String) : RecoverableParseException(message)

class NoAcceptableTypesException(message: String) : RecoverableParseException(message)

class NoResourceFoundException(message: String) : RecoverableParseException(message)

class UnableToParseException(message: String) : UnrecoverableParseException(message)
