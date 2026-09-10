package id.darno.core.exceptions.repository

class DatabaseSyntaxException(
    message: String = "Kesalahan pada perintah SQL",
    cause: Throwable? = null
) : DatabaseException(
    message = message,
    cause = cause
)