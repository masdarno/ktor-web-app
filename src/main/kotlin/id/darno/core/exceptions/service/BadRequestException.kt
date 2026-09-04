package id.darno.core.exceptions.service

class BadRequestException(
    message: String = "Invalid request"
) : ServiceException(message)