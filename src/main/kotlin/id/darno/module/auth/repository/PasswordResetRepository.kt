package id.darno.module.auth.repository

import id.darno.module.auth.model.PasswordResetToken
import kotlinx.datetime.LocalDateTime

interface PasswordResetRepository {
    suspend fun save(
        tokenHash: String,
        userId: Short,
        expiresAt: LocalDateTime
    )

    suspend fun findValid(tokenHash: String): PasswordResetToken?
    suspend fun markUsed(tokenHash: String)
    suspend fun deleteByUserId(userId: Short)
}
