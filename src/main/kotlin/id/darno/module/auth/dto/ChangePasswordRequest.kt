package id.darno.module.auth.dto

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val passwordConfirmation: String
)