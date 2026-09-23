package id.darno.module.auth.dto

data class RegisterRequest(
    val nama: String,
    val username: String,
    val password: String,
    val passwordConfirmation: String,
    val email: String
)