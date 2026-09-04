package id.darno.module.auth.service

interface EmailVerificationService {
    suspend fun sendVerification(userId: Short, email: String)
    suspend fun verify(token: String): Result<Unit>
}