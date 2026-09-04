package id.darno.module.role.repository

import id.darno.core.database.DbExceptionMapper
import id.darno.core.database.dbQuery
import id.darno.module.role.database.dao.RoleEntity
import id.darno.module.role.database.table.RoleTable
import id.darno.module.role.domain.RoleDomain
import id.darno.module.role.mapper.toRoleDomain
import id.darno.module.role.model.RoleCreateParams
import id.darno.module.role.model.RoleUpdateParams
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException

class RoleRepositoryImpl : RoleRepository {

    override suspend fun findAll(): List<RoleDomain> = dbQuery {
        RoleEntity.all().map { it.toRoleDomain() }
    }

    override suspend fun findById(id: Short): RoleDomain? = dbQuery {
        RoleEntity.findById(id)?.toRoleDomain()
    }
    override suspend fun existsById(id: Short): Boolean = dbQuery {
        RoleEntity.find { RoleTable.id eq id }.any()
    }

    override suspend fun existsByName(name: String): Boolean = dbQuery {
        RoleEntity.find { RoleTable.name eq name }
            .any()
    }

    override suspend fun create(params: RoleCreateParams): RoleDomain = dbQuery {
        try {
            RoleEntity.new {
                name = params.name
                isActive = params.isActive
            }.toRoleDomain()
        } catch (e: ExposedSQLException) {
            throw DbExceptionMapper.map(e)
        }
    }

    override suspend fun update(id: Short, params: RoleUpdateParams): RoleDomain = dbQuery {
        try {
            val entity = RoleEntity[id]
            entity.apply {
                name = params.name ?: name
                isActive = params.isActive ?: isActive
            }.toRoleDomain()
        } catch (e: ExposedSQLException) {
            throw DbExceptionMapper.map(e)
        }
    }

    override suspend fun delete(id: Short): Boolean = dbQuery {
        try {
            RoleEntity[id].delete()
            true
        } catch (e: ExposedSQLException) {
            throw DbExceptionMapper.map(e)
        }
    }
}
