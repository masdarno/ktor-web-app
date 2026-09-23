package id.darno.core.exceptions.service

class NotFoundException(
    message: String = "Resource not found"
) : ServiceException(message)