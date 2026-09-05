package id.darno.module.user.repository

import id.darno.core.database.DbExceptionMapper
import id.darno.core.database.dbQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.auth.model.UserCredentials
import id.darno.module.role.database.dao.RoleEntity
import id.darno.module.role.database.table.RoleTable
import id.darno.module.unit.database.dao.toUnitDomain
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.user.config.PhotoUrlConfig
import id.darno.module.user.database.dao.UserEntity
import id.darno.module.user.database.table.UserTable
import id.darno.module.user.database.table.UserUnitTable
import id.darno.module.user.domain.UserDomain
import id.darno.module.user.mapper.toUserCredentials
import id.darno.module.user.mapper.toUserDomain
import id.darno.module.user.model.CreateUserParams
import id.darno.module.user.model.UpdateUserParams
import id.darno.module.user.model.UserListItem
import id.darno.module.user.model.UserOptionItem
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.slf4j.LoggerFactory

class UserRepositoryImpl(private val config: PhotoUrlConfig) : UserRepository {

    private val logger = LoggerFactory.getLogger(UserRepository::class.java)

    // --- C: CREATE (Membuat User Baru) ---
    override suspend fun create(params: CreateUserParams): UserDomain = dbQuery {
        logger.info("Create user with name: {}", params.nama)
        try {
            UserEntity.new {
                nama = params.nama
                alias = params.nama
                username = params.username
                password = params.password
                email = params.email
                genderId = params.genderId
                role = RoleEntity[params.roleId]
            }.toUserDomain(config)
        } catch (e: ExposedSQLException) {
            throw DbExceptionMapper.map(e)
        }
    }

    // --- R: READ (Mencari berdasarkan Username) ---
    override suspend fun findByUsername(username: String): UserDomain? = dbQuery {
        UserEntity.find { UserTable.username eq username }
            .singleOrNull()
            ?.toUserDomain(config)
    }

    override suspend fun existsByUsername(username: String): Boolean = dbQuery{
        UserEntity.find { UserTable.username eq username }
            .empty()
            .not()
    }

    // --- R: READ (Mencari berdasarkan Email) ---
    override suspend fun findByEmail(email: String): UserDomain? = dbQuery {
        UserEntity.find { UserTable.email eq email }
            .singleOrNull()
            ?.toUserDomain(config)
    }

    override suspend fun existsByEmail(email: String): Boolean = dbQuery{
        UserEntity.find { UserTable.email eq email }
            .any() // sama dengan .empty().not()
    }

    // Untuk Login
    override suspend fun findCredentialsByUsername(username: String): UserCredentials? = dbQuery {
        UserEntity.find { UserTable.username eq username }
            .firstOrNull()
            ?.toUserCredentials()
    }

    override suspend fun findCredentialsById(id: Short): UserCredentials? = dbQuery {
        UserEntity.find { UserTable.id eq id }
            .firstOrNull()
            ?.toUserCredentials()
    }

    // --- R: READ (Mencari berdasarkan ID) ---
    override suspend fun findById(id: Short): UserDomain? = dbQuery {
        UserEntity.findById(id)?.toUserDomain(config)
    }

    // --- U: UPDATE (Memperbarui User) ---
    override suspend fun update(id: Short, params: UpdateUserParams): UserDomain = dbQuery {
        try {
            val entity = UserEntity[id] // PRECONDITION: user exists

            entity.apply {
                nama = params.nama ?: nama
                alias = params.alias ?: alias
                username = params.username ?: username
                password = params.password ?: password
                email = params.email ?: email
                genderId = params.genderId ?: genderId
                photo = params.photo ?: photo
                isActive = params.isActive ?: isActive
                params.roleId?.let { role = RoleEntity[it] }
            }.toUserDomain(config)

        } catch (e: ExposedSQLException) {
            throw DbExceptionMapper.map(e)
        }
    }

    // --- D: DELETE (Menghapus User) ---
    override suspend fun delete(id: Short): Boolean = dbQuery {
        try {
            UserTable.deleteWhere { UserTable.id eq id }
            true
        } catch (e: ExposedSQLException) {
            throw DbExceptionMapper.map(e)
        }
    }

    override suspend fun markEmailVerified(userId: Short) {
        dbQuery {
            UserTable.update({ UserTable.id eq userId }) {
                it[emailVerifiedAt] =
                    Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
        }
    }

    override suspend fun findUnitsByUserId(userId: Short): List<UnitDomain> = dbQuery{
        // userId sudah dipastikan keberadaannya di service
        UserEntity[userId].units.map { it.toUnitDomain() }
    }

    override suspend fun userHasUnit(
        userId: Short,
        unitId: Short
    ): Boolean = dbQuery {
        UserUnitTable
            .selectAll()
            .where {
                (UserUnitTable.userId eq userId) and
                        (UserUnitTable.unitId eq unitId)
            }
            .limit(1)
            .any()
    }

    override suspend fun findAll(
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String
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

        // --- 2. Filtering ---
        val filter = search?.let {
            (UserTable.nama like "%$it%") or
                    (UserTable.username like "%$it%") or
                    (UserTable.email like "%$it%")
        }

        // --- 3. Hitung total data ---
        val total = UserTable
            .leftJoin(RoleTable)
            .selectAll()
            .let { if (filter != null) it.where(filter) else it }
            .count()

        val totalPages = if (total == 0L) 1 else ((total + pageSize - 1) / pageSize).toInt()

        val offset = (page - 1) * pageSize

        // --- 4. Query utama (pagination + sorting) ---
        val data = UserTable
            .leftJoin(RoleTable)
            .select(
                UserTable.id,
                UserTable.nama,
                UserTable.username,
                UserTable.email,
                RoleTable.nama
            )
            .let { if (filter != null) it.where { filter } else it }
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

        // --- 5. Kembalikan hasil ---
        PagedResult(
            data = data,
            page = page,
            pageSize = pageSize,
            total = total,
            totalPages = totalPages
        )
    }

    override suspend fun findAllByUnit(
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
    override suspend fun findAvailableForUnit(
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