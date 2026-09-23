package id.darno.core.exceptions.repository

class DatabaseTimeoutException(
    message: String = "Database timeout",
    cause: Throwable? = null
) : DatabaseException(
    message = message,
    cause = cause
)