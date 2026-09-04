package id.darno.module.unit.database.dao

import id.darno.module.unit.database.table.UnitTable
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.user.database.dao.UserEntity
import id.darno.module.user.database.table.UserUnitTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID

class UnitEntity(id: EntityID<Short>) : Entity<Short>(id) {
    companion object : EntityClass<Short, UnitEntity>(UnitTable)
    var nama by UnitTable.nama
    var isActive by UnitTable.isActive
    var createdAt by UnitTable.createdAt
    var updatedAt by UnitTable.updatedAt

    // Relasi many-to-many dengan User
    var users by UserEntity.Companion via UserUnitTable
}

fun UnitEntity.toUnitDomain(): UnitDomain {
    return UnitDomain(
        id = id.value,
        nama = nama
    )
}