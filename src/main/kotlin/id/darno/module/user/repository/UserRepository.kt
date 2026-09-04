package id.darno.module.user.repository

import id.darno.core.pageddata.model.PagedResult
import id.darno.module.auth.model.UserCredentials
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.user.domain.UserDomain
import id.darno.module.user.model.CreateUserParams
import id.darno.module.user.model.UpdateUserParams
import id.darno.module.user.model.UserListItem

interface UserRepository {
    suspend fun create(params: CreateUserParams): UserDomain
    suspend fun findByUsername(username: String): UserDomain?
    suspend fun existsByUsername(username: String): Boolean
    suspend fun findByEmail(email: String): UserDomain?
    suspend fun existsByEmail(email: String): Boolean
    suspend fun findCredentialsByUsername(username: String): UserCredentials?
    suspend fun findCredentialsById(id: Short): UserCredentials?
    suspend fun findById(id: Short): UserDomain?
    suspend fun update(id: Short, params: UpdateUserParams): UserDomain
    suspend fun delete(id: Short): Boolean

    suspend fun markEmailVerified(userId: Short)

    suspend fun findUnitsByUserId(userId: Short): List<UnitDomain>
    suspend fun userHasUnit(
        userId: Short,
        unitId: Short
    ): Boolean

    // Untuk tabel user join role dengan filter, sort, pagination
    suspend fun findAll(
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String
    ): PagedResult<UserListItem>

}
