package id.darno.module.user.service

import id.darno.core.pageddata.model.PagedQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.core.report.ReportFile
import id.darno.core.report.ReportFormat
import id.darno.module.user.model.UserListItem
import id.darno.module.user.model.UserOptionItem

interface UserUnitService {

    suspend fun getUserUnitTable(
        query: PagedQuery,
        unitId: Short
    ): PagedResult<UserListItem>
    suspend fun getAvailableUsersForUnit(
        unitId: Short,
        search: String? = null
    ): List<UserOptionItem>
    suspend fun addUsersToUnit(
        unitId: Short,
        userIds: List<Short>
    ): Int
    suspend fun deleteUserUnit(
        userId: Short,
        unitId: Short
    ): Boolean
    suspend fun generateReport(unitId: Short, search: String?, format: ReportFormat = ReportFormat.PDF): ReportFile
}