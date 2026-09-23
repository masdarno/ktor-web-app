package id.darno.core.exceptions.repository

open class DatabaseException(
    message: String,
    cause: Throwable? = null
) : RepositoryException(message, cause)