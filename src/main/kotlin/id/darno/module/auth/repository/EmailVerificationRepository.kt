package id.darno.module.auth.repository

import id.darno.module.auth.model.EmailVerificationToken

interface EmailVerificationRepository {

    suspend fun find(token: String): EmailVerificationToken?

    suspend fun save(
        token: String,
        userId: Short
    )

    suspend fun delete(token: String)

    suspend fun deleteByUserId(userId: Short)
}
