package id.darno.module.user.service

import id.darno.core.pageddata.model.PagedQuery
import id.darno.core.report.JasperReportService
import id.darno.core.report.ReportFile
import id.darno.core.report.ReportFormat
import id.darno.module.user.model.UserOptionItem
import id.darno.module.user.repository.UserUnitRepository

class UserUnitServiceImpl(
    private val userUnitRepository: UserUnitRepository,
    private val jasperReportService: JasperReportService
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

    override suspend fun generateReport(
        unitId: Short,
        search: String?,
        format: ReportFormat
    ): ReportFile {

        val rows =
            userUnitRepository.findAllUserByUnitForReport(
                unitId,
                search
            )

        return jasperReportService.generate(
            reportPath = "reports/user-unit.jasper",
            format = format,
            data = rows,
            parameters = mapOf(
                "UNIT_ID" to unitId,
                "REPORT_TITLE" to "Daftar User Unit",
            ),
            fileName = "user-unit"
        )
    }
}