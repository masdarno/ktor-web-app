package id.darno.module.role.repository

import id.darno.core.database.query.DatabaseQuery
import id.darno.module.role.database.dao.RoleEntity
import id.darno.module.role.database.table.RoleTable
import id.darno.module.role.domain.RoleDomain
import id.darno.module.role.mapper.toRoleDomain
import id.darno.module.role.model.RoleCreateParams
import id.darno.module.role.model.RoleUpdateParams
import org.jetbrains.exposed.v1.core.eq

class RoleRepositoryImpl(
    private val databaseQuery: DatabaseQuery
) : RoleRepository {

    override suspend fun findAll(): List<RoleDomain> = databaseQuery {
        RoleEntity.all().map { it.toRoleDomain() }
    }

    override suspend fun findById(id: Short): RoleDomain? = databaseQuery {
        RoleEntity.findById(id)?.toRoleDomain()
    }
    override suspend fun existsById(id: Short): Boolean = databaseQuery {
        RoleEntity.find { RoleTable.id eq id }.any()
    }

    override suspend fun existsByName(nama: String): Boolean = databaseQuery {
        RoleEntity.find { RoleTable.nama eq nama }
            .any()
    }

    override suspend fun create(params: RoleCreateParams): RoleDomain = databaseQuery {
        RoleEntity.new {
            nama = params.nama
            isActive = params.isActive
        }.toRoleDomain()
    }

    override suspend fun update(id: Short, params: RoleUpdateParams): RoleDomain = databaseQuery {
        val entity = RoleEntity[id]
        entity.apply {
            nama = params.nama ?: nama
            isActive = params.isActive ?: isActive
        }.toRoleDomain()
    }

    override suspend fun delete(id: Short): Boolean = databaseQuery {
        RoleEntity[id].delete()
        true
    }
}
