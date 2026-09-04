package id.darno.module.user.service

import id.darno.module.user.repository.UserRepository

class UserEmailVerificationServiceImpl(
    private val userRepository: UserRepository
): UserEmailVerificationService {
    override suspend fun markEmailVerified(id: Short) {
        userRepository.markEmailVerified(id)
    }
}