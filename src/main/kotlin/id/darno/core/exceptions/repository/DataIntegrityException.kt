package id.darno.core.exceptions.repository

class DataIntegrityException(
    message: String,
    cause: Throwable? = null
) : RepositoryException(message, cause)