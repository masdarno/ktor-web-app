package id.darno.core.exceptions

open class ApplicationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)