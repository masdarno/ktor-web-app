package id.darno.core.exceptions.service

import id.darno.core.exceptions.ApplicationException

open class ServiceException (
    message: String,
    cause: Throwable? = null
) : ApplicationException(message, cause)