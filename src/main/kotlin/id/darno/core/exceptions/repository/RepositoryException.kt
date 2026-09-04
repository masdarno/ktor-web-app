package id.darno.core.exceptions.repository

import id.darno.core.exceptions.ApplicationException

open class RepositoryException (
    message: String,
    cause: Throwable? = null
) : ApplicationException(message, cause)