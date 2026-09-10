package id.darno.core.exceptions.repository

class CheckConstraintException(
    val constraint: String? = null,
    message: String = "Data tidak memenuhi constraint",
    cause: Throwable? = null
) : DataIntegrityException(
    message = message,
    cause = cause
)