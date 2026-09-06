package id.darno.module.user.service

import id.darno.module.user.domain.UserDomain
import id.darno.module.user.model.CreateUserParams

interface UserProvisioningService {

    suspend fun createUserWithUnit(
        params: CreateUserParams,
        unitId: Short
    ): UserDomain
}