package id.darno.module.auth.dto

data class ResetPasswordRequest(
    val token: String,
    val password: String,
    val passwordConfirmation: String
)