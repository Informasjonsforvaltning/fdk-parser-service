package no.digdir.fdk.parseservice.exception

class RecoverableParseException(message: String) : Exception(message)

class UnrecoverableParseException(message: String) : Exception(message)
