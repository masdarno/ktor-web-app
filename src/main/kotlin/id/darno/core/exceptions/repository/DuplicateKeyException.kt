package id.darno.core.exceptions.repository

class DuplicateKeyException(
    val field: String,
    cause: Throwable? = null
) : RepositoryException("Duplicate $field", cause)