package id.darno.module.auth.exception

sealed class AuthException(message: String) : RuntimeException(message) {
    class TokenNotFound : AuthException("Token tidak ditemukan")
    class TokenInvalid : AuthException("Token tidak valid")
    class UserUnitInvalid : AuthException("User unit tidak valid")
}
