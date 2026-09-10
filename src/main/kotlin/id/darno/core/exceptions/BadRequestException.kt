package id.darno.core.exceptions

class BadRequestException(
    message: String = "Invalid request"
) : ApplicationException(message)