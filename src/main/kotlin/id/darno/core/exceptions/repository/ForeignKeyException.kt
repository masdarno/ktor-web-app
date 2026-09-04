package id.darno.core.exceptions.repository

class ForeignKeyException(
    val reference: String,
    cause: Throwable? = null
) : RepositoryException("Invalid reference: $reference", cause)