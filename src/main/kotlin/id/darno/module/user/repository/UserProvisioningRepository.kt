package id.darno.module.user.repository

import id.darno.module.user.domain.UserDomain
import id.darno.module.user.model.CreateUserParams

interface UserProvisioningRepository {

    suspend fun createUserWithUnit(
        params: CreateUserParams,
        unitId: Short
    ): UserDomain
}