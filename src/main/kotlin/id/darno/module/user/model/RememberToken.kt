package id.darno.module.user.model

import kotlinx.datetime.LocalDateTime

data class RememberToken(
    val selector: String,
    val userId: Short,
    val unitId: Short,
    val validatorHash: String,
    val expiresAt: LocalDateTime
)