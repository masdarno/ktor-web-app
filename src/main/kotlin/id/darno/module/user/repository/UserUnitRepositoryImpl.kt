package id.darno.module.user.repository

import id.darno.core.database.DbExceptionMapper
import id.darno.core.database.dbQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.role.database.table.RoleTable
import id.darno.module.user.database.table.UserTable
import id.darno.module.user.database.table.UserUnitTable
import id.darno.module.user.model.UserListItem
import id.darno.module.user.model.UserOptionItem
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.notInSubQuery
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

class UserUnitRepositoryImpl: UserUnitRepository {

    override suspend fun findAllUserByUnit(
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String,
        unitId: Short
    ): PagedResult<UserListItem> = dbQuery {

        // --- 1. Tentukan sort column ---
        val sortColumn = when (sortBy) {
            "name" -> UserTable.nama
            "username" -> UserTable.username
            "email" -> UserTable.email
            "role" -> RoleTable.nama
            else -> UserTable.id
        }

        val order = when (sortDir.lowercase()) {
            "asc" -> SortOrder.ASC
            else -> SortOrder.DESC
        }

        // --- 2. Buat kondisi filter ---
        val searchFilter = search
            ?.takeIf { it.isNotBlank() }
            ?.let {
                (UserTable.nama like "%$it%") or
                        (UserTable.username like "%$it%") or
                        (UserTable.email like "%$it%")
            }

        val unitFilter = unitId?.let {
            UserUnitTable.unitId eq it
        }

        // Gabungkan filter
        val filter = listOfNotNull(
            searchFilter,
            unitFilter
        ).reduceOrNull { acc, condition ->
            acc and condition
        }

        // --- 3. FROM / JOIN ---
        val baseQuery =
            if (unitId != null) {
                UserTable
                    .innerJoin(UserUnitTable)
                    .leftJoin(RoleTable)
            } else {
                UserTable
                    .leftJoin(RoleTable)
            }

        // --- 4. Hitung total ---
        val total = baseQuery
            .selectAll()
            .let {
                if (filter != null) {
                    it.where { filter }
                } else {
                    it
                }
            }
            .count()

        val totalPages =
            if (total == 0L) {
                1
            } else {
                ((total + pageSize - 1) / pageSize).toInt()
            }

        val offset = (page - 1) * pageSize

        // --- 5. Query data ---
        val data = baseQuery
            .select(
                UserTable.id,
                UserTable.nama,
                UserTable.username,
                UserTable.email,
                RoleTable.nama
            )
            .let {
                if (filter != null) {
                    it.where { filter }
                } else {
                    it
                }
            }
            .orderBy(sortColumn to order)
            .limit(pageSize)
            .offset(offset.toLong())
            .map {
                UserListItem(
                    id = it[UserTable.id].value,
                    nama = it[UserTable.nama],
                    username = it[UserTable.username],
                    email = it[UserTable.email],
                    roleName = it[RoleTable.nama]
                )
            }

        // --- 6. Result ---
        PagedResult(
            data = data,
            page = page,
            pageSize = pageSize,
            total = total,
            totalPages = totalPages
        )
    }

    // Cari user yang BELUM terdaftar pada unit tertentu (anti-join via notInSubQuery)
    override suspend fun findAvailableUserForUnit(
        unitId: Short,
        search: String?
    ): List<UserOptionItem> = dbQuery {

        val assignedUserIds = UserUnitTable
            .select(UserUnitTable.userId)
            .where { UserUnitTable.unitId eq unitId }

        val notAssignedCondition = UserTable.id notInSubQuery assignedUserIds

        val searchFilter = search
            ?.takeIf { it.isNotBlank() }
            ?.let {
                (UserTable.nama like "%$it%") or
                        (UserTable.username like "%$it%")
            }

        val condition = searchFilter
            ?.let { notAssignedCondition and it }
            ?: notAssignedCondition

        UserTable
            .select(UserTable.id, UserTable.nama, UserTable.username)
            .where { condition }
            .orderBy(UserTable.nama to SortOrder.ASC)
            .map {
                UserOptionItem(
                    id = it[UserTable.id].value,
                    nama = it[UserTable.nama],
                    username = it[UserTable.username]
                )
            }
    }

    // Insert banyak user ke user_units sekaligus.
    // ignore = true -> aman kalau ada baris yang ternyata sudah pernah ditambahkan
    // (primary key composite userId+unitId akan diabaikan alih-alih error).
    override suspend fun addUserUnits(
        unitId: Short,
        userIds: List<Short>
    ): Int = dbQuery {
        if (userIds.isEmpty()) return@dbQuery 0

        try {
            UserUnitTable.batchInsert(
                data = userIds.distinct(),
                ignore = true,
                shouldReturnGeneratedValues = false
            ) { userId ->
                this[UserUnitTable.userId] = userId
                this[UserUnitTable.unitId] = unitId
            }.size
        } catch (e: ExposedSQLException) {
            throw DbExceptionMapper.map(e)
        }
    }

    override suspend fun deleteUserUnit(
        userId: Short,
        unitId: Short
    ): Boolean = dbQuery {
        try {
            UserUnitTable
                .deleteWhere {
                    (UserUnitTable.userId eq userId) and
                            (UserUnitTable.unitId eq unitId)
                } > 0
            true
        } catch (e: ExposedSQLException) {
            throw DbExceptionMapper.map(e)
        }
    }

}