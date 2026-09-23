package id.darno.core.exceptions.service

class ConflictException(
    message: String = "Conflict occurred"
) : ServiceException(message)