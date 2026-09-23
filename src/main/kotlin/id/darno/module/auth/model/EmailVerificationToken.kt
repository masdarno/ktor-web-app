package id.darno.module.auth.model

import kotlinx.datetime.LocalDateTime

data class EmailVerificationToken(
    val token: String,
    val userId: Short,
    val expiresAt: LocalDateTime
)