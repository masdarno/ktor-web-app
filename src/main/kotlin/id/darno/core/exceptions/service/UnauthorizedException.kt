package id.darno.core.exceptions.service

class UnauthorizedException(
    message: String = "Unauthorized"
) : ServiceException(message)