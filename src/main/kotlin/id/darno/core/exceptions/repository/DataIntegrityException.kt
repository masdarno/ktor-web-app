package id.darno.core.exceptions.repository

open class DataIntegrityException(
    message: String,
    cause: Throwable? = null
) : DatabaseException(message, cause)