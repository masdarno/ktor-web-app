package id.darno.core.exceptions

class ForbiddenAccessException(
    message: String = "Anda tidak memiliki akses ke halaman ini"
) : RuntimeException(message)