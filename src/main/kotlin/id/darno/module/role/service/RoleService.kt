package id.darno.module.role.service

import id.darno.module.role.domain.RoleDomain
import id.darno.module.role.model.RoleCreateParams
import id.darno.module.role.model.RoleUpdateParams

interface RoleService {

    suspend fun getAll(): List<RoleDomain>

    suspend fun getById(id: Short): RoleDomain

    suspend fun create(params: RoleCreateParams): RoleDomain

    suspend fun update(id: Short, params: RoleUpdateParams): RoleDomain

    suspend fun delete(id: Short): Boolean
}
