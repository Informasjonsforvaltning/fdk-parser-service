package no.digdir.fdk.parseservice.model

class NoAcceptableFDKRecordsException(message: String) : Exception(message)

class MultipleFDKRecordsException(message: String) : Exception(message)

class NoAcceptableTypesException(message: String) : Exception(message)

class NoResourceFoundException(message: String) : Exception(message)

class UnableToParseException(message: String) : Exception(message)
