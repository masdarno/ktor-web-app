package id.darno.module.user.service

import id.darno.module.unit.domain.UnitDomain
import id.darno.module.user.domain.UserDomain

class UserLookupServiceImpl(
    private val userService: UserService
) : UserLookupService {
    override suspend fun getById(id: Short): UserDomain {
        // Tambahkan logic khusus untuk lookup, misalnya caching
        println("Looking up user with id: $id")
        return userService.getById(id)
    }

    override suspend fun getByEmail(email: String): UserDomain? {
        return userService.getByEmail(email)
    }

    override suspend fun getUnitsForUser(userId: Short): List<UnitDomain> {
        return userService.getUnitsForUser(userId)
    }

    override suspend fun userHasUnit(userId: Short, unitId: Short): Boolean {
        return userService.userHasUnit(userId, unitId)
    }
}