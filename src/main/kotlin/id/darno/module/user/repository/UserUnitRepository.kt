package id.darno.module.user.repository

import id.darno.core.pageddata.model.PagedResult
import id.darno.module.user.model.UserListItem
import id.darno.module.user.model.UserOptionItem

interface UserUnitRepository {

    suspend fun findAllUserByUnit(
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String,
        unitId: Short
    ): PagedResult<UserListItem>

    suspend fun findAvailableUserForUnit(
        unitId: Short,
        search: String? = null
    ): List<UserOptionItem>

    suspend fun addUserUnits(
        unitId: Short,
        userIds: List<Short>
    ): Int

    suspend fun deleteUserUnit(
        userId: Short,
        unitId: Short
    ): Boolean

}