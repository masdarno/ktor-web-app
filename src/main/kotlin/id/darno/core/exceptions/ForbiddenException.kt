package id.darno.core.exceptions

class ForbiddenException(
    message: String = "Anda tidak memiliki akses ke halaman ini"
) : ApplicationException(message)