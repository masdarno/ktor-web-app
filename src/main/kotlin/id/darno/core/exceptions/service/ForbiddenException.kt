package id.darno.core.exceptions.service

class ForbiddenException(
    message: String = "Forbidden"
) : ServiceException(message)