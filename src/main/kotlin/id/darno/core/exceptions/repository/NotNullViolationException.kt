package id.darno.core.exceptions.repository

class NotNullViolationException(
    val constraint: String? = null,
    message: String = "Field wajib tidak boleh kosong",
    cause: Throwable? = null
) : DataIntegrityException(
    message = message,
    cause = cause
)