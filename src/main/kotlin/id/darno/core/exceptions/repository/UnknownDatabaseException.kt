package id.darno.core.exceptions.repository

class UnknownDatabaseException(
    message: String = "Terjadi kesalahan database",
    cause: Throwable? = null
) : DatabaseException(
    message = message,
    cause = cause
)