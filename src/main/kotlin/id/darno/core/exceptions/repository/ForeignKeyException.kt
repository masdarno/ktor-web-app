package id.darno.core.exceptions.repository

class ForeignKeyException(
    val constraint: String? = null,
    message: String = "Referensi data tidak valid",
    cause: Throwable? = null
) : DataIntegrityException(
    message = message,
    cause = cause
)