package id.darno.module.auth.model

import kotlinx.datetime.LocalDateTime

data class PasswordResetToken(
    val tokenHash: String,
    val userId: Short,
    val expiresAt: LocalDateTime
)
