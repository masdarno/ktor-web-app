package id.darno.module.role.repository

import id.darno.module.role.domain.RoleDomain
import id.darno.module.role.model.RoleCreateParams
import id.darno.module.role.model.RoleUpdateParams

interface RoleRepository {

    suspend fun findAll(): List<RoleDomain>

    suspend fun findById(id: Short): RoleDomain?

    suspend fun existsByName(name: String): Boolean

    suspend fun create(params: RoleCreateParams): RoleDomain

    suspend fun update(id: Short, params: RoleUpdateParams): RoleDomain

    suspend fun delete(id: Short): Boolean
    suspend fun existsById(id: Short): Boolean
}
