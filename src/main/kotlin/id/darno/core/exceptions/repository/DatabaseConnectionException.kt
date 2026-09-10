package id.darno.core.exceptions.repository

class DatabaseConnectionException(
    message: String = "Koneksi database bermasalah",
    cause: Throwable? = null
) : DatabaseException(
    message = message,
    cause = cause
)