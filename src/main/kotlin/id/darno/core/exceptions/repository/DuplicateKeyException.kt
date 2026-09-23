package id.darno.core.exceptions.repository

class DuplicateKeyException(
    val constraint: String? = null,
    message: String = "Data sudah ada / duplikat",
    cause: Throwable? = null
) : DataIntegrityException(
    message = message,
    cause = cause
)