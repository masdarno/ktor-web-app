package id.darno.module.role.database.dao

import id.darno.module.role.database.table.RoleTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID

class RoleEntity(id: EntityID<Short>) : Entity<Short>(id) {
    companion object : EntityClass<Short, RoleEntity>(RoleTable)
    var name by RoleTable.name
    var isActive by RoleTable.isActive
    var createdAt by RoleTable.createdAt
    var updatedAt by RoleTable.updatedAt
}