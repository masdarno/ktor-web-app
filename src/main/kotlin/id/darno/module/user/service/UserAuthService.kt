package id.darno.module.user.service

import id.darno.module.auth.model.AuthResult
import id.darno.module.auth.model.LoginParams
import id.darno.module.user.domain.UserDomain
import id.darno.module.user.model.CreateUserParams

interface UserAuthService {
    suspend fun register(params: CreateUserParams): UserDomain
    suspend fun verifyCredentials(params: LoginParams): AuthResult
    suspend fun resetPassword(
        userId: Short,
        password: String
    ): UserDomain
    suspend fun changePassword(
        userId: Short,
        currentPassword: String,
        newPassword: String
    )
}