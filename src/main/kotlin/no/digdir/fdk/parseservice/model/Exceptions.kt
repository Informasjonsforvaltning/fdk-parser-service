package no.digdir.fdk.parseservice.model

class NoAcceptableFDKRecordsException(message: String) : Exception(message)

class MultipleFDKRecordsException(message: String) : Exception(message)

class NoPrimaryTopicOnFDKRecordException(message: String) : Exception(message)

class MoAcceptableTopicsOnFDKRecordException(message: String) : Exception(message)
