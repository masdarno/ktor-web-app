package id.darno.core.exceptions.repository

class DatabaseConcurrencyException(
    message: String = "Operasi database mengalami konflik concurrency",
    cause: Throwable? = null
) : DatabaseException(
    message = message,
    cause = cause
)