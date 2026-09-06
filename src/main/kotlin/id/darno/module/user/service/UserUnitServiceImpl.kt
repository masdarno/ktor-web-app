package id.darno.module.user.service

import id.darno.core.pageddata.model.PagedQuery
import id.darno.module.user.model.UserOptionItem
import id.darno.module.user.repository.UserUnitRepository

class UserUnitServiceImpl(
    private val userUnitRepository: UserUnitRepository
): UserUnitService {
    override suspend fun getUserUnitTable(
        query: PagedQuery,
        unitId: Short
    ) =
        userUnitRepository.findAllUserByUnit(
            search = query.search,
            page = query.page,
            pageSize = query.pageSize,
            sortBy = query.sortBy,
            sortDir = query.sortDir,
            unitId = unitId
        )

    override suspend fun getAvailableUsersForUnit(
        unitId: Short,
        search: String?
    ): List<UserOptionItem> =
        userUnitRepository.findAvailableUserForUnit(unitId, search)

    override suspend fun addUsersToUnit(
        unitId: Short,
        userIds: List<Short>
    ): Int =
        userUnitRepository.addUserUnits(unitId, userIds)

    override suspend fun deleteUserUnit(
        userId: Short,
        unitId: Short
    ) =
        userUnitRepository.deleteUserUnit(
            userId = userId,
            unitId = unitId
        )
}