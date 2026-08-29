package com.sleepy.readify.core.model

sealed class DomainError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    class RuleNotFound(message: String) : DomainError(message)

    class RuleInvalid(message: String) : DomainError(message)

    class NetworkError(message: String, cause: Throwable? = null) : DomainError(message, cause)

    class ExtractionLowQuality(message: String) : DomainError(message)

    class StorageError(message: String, cause: Throwable? = null) : DomainError(message, cause)

    class ExportError(message: String, cause: Throwable? = null) : DomainError(message, cause)
}
