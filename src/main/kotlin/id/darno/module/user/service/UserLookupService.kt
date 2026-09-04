package id.darno.module.user.service

import id.darno.module.unit.domain.UnitDomain
import id.darno.module.user.domain.UserDomain

interface UserLookupService {
    suspend fun getById(id: Short): UserDomain
    suspend fun getByEmail(email: String): UserDomain?
    suspend fun getUnitsForUser(userId: Short): List<UnitDomain>
    suspend fun userHasUnit(userId: Short, unitId: Short): Boolean
}