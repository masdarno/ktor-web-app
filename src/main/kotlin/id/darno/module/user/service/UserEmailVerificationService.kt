package id.darno.module.user.service

interface UserEmailVerificationService {
    suspend fun markEmailVerified(id: Short)
}