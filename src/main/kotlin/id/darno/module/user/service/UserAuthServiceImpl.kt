package id.darno.module.user.service

import id.darno.core.exceptions.ApplicationException
import id.darno.core.security.crypto.Hasher
import id.darno.module.auth.model.AuthResult
import id.darno.module.auth.model.LoginParams
import id.darno.module.user.domain.UserDomain
import id.darno.module.user.model.CreateUserParams
import id.darno.module.user.model.UpdateUserParams
import id.darno.module.user.repository.UserRepository

class UserAuthServiceImpl(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val hasher: Hasher
) : UserAuthService {

    override suspend fun register(params: CreateUserParams): UserDomain {
        return userService.create(params)
    }

    override suspend fun verifyCredentials(params: LoginParams): AuthResult {

        // Ambil pengguna dari database berdasarkan username
        val credentials = userRepository.findCredentialsByUsername(params.username)
            ?: return AuthResult.InvalidCredentials // User tidak ditemukan

        // Password salah
        if( ! hasher.verify(params.password, credentials.passwordHash)){
            return AuthResult.InvalidCredentials
        }

        // User ditemukan, password sesuai tapi tidak aktif
        if (!credentials.isActive) {
            return AuthResult.UserInactive
        }
        // User sukes login
        val user = userRepository.findById(credentials.id)
            ?: return AuthResult.InvalidCredentials // fallback, shouldn't happen

        val units = userRepository.findUnitsByUserId(user.id)

        // user pasti ada, units boleh null
        return AuthResult.Success(user, units)
    }

    override suspend fun resetPassword(userId: Short, password: String): UserDomain{
        val param = UpdateUserParams(
            password = hasher.hash(password)
        )
        return userRepository.update(userId, param)
    }

    override suspend fun changePassword(
        userId: Short,
        currentPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findCredentialsById(userId)
            ?: throw ApplicationException("User tidak ditemukan")

        if (!hasher.verify(currentPassword, user.passwordHash)) {
            throw ApplicationException("Password saat ini salah")
        }

        val param = UpdateUserParams(
            password = hasher.hash(newPassword)
        )
        userRepository.update(userId, param)

    }

}