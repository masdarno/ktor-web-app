package id.darno.module.user.service

import id.darno.core.pageddata.model.PagedQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.user.domain.UserDomain
import id.darno.module.user.model.CreateUserParams
import id.darno.module.user.model.UpdateUserParams
import id.darno.module.user.model.UserListItem
import id.darno.module.user.model.UserOptionItem

interface UserService {
    suspend fun create(params: CreateUserParams): UserDomain
    suspend fun getById(id: Short): UserDomain
    suspend fun getByEmail(email: String): UserDomain?
    suspend fun update(id: Short, params: UpdateUserParams): UserDomain
    suspend fun delete(id: Short): Boolean
    suspend fun getUnitsForUser(userId: Short): List<UnitDomain>
    suspend fun userHasUnit(userId: Short, unitId: Short): Boolean
    suspend fun getUserTable(query: PagedQuery): PagedResult<UserListItem>
    suspend fun getAvailableUsersForUnit(
        unitId: Short,
        search: String? = null
    ): List<UserOptionItem>
    suspend fun addUsersToUnit(
        unitId: Short,
        userIds: List<Short>
    ): Int
    suspend fun getUserUnitTable(
        query: PagedQuery,
        unitId: Short
    ): PagedResult<UserListItem>
    suspend fun deleteUserUnit(
        userId: Short,
        unitId: Short
    ): Boolean
}
