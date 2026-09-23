package id.darno.module.user.service

import id.darno.core.exceptions.service.ConflictException
import id.darno.core.security.crypto.Hasher
import id.darno.module.user.domain.UserDomain
import id.darno.module.user.model.CreateUserParams
import id.darno.module.user.repository.UserProvisioningRepository
import id.darno.module.user.repository.UserRepository

class UserProvisioningServiceImpl(
    private val userRepository: UserRepository,
    private val provisioningRepository: UserProvisioningRepository,
    private val hasher: Hasher
) : UserProvisioningService {

    override suspend fun createUserWithUnit(
        params: CreateUserParams,
        unitId: Short
    ): UserDomain {

        // Sama dengan validasi UserService.create()
        if (userRepository.existsByUsername(params.username)) {
            throw ConflictException(
                "Username ${params.username} sudah ada"
            )
        }

        if (userRepository.existsByEmail(params.email)) {
            throw ConflictException(
                "Email ${params.email} sudah ada"
            )
        }

        // Password plain text hanya sampai di sini.
        val hashedPassword = hasher.hash(params.password)

        val secureParams = params.copy(
            password = hashedPassword
        )

        return provisioningRepository.createUserWithUnit(
            params = secureParams,
            unitId = unitId
        )
    }
}