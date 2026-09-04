package id.darno.module.auth.service

interface PasswordResetService {
    suspend fun requestReset(email: String)
    suspend fun isTokenValid(token: String): Boolean
    suspend fun resetPassword(token: String, newPassword: String): Boolean
}
