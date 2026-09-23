package id.darno.module.user.database.dao

import id.darno.module.role.database.dao.RoleEntity
import id.darno.module.unit.database.dao.UnitEntity
import id.darno.module.user.database.table.UserTable
import id.darno.module.user.database.table.UserUnitTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID

/**
 * Entity Class yang memetakan ke UserTable
 */
class UserEntity(id: EntityID<Short>) : Entity<Short>(id) {
    // Companion object ini harus mendefinisikan tabel yang digunakannya
    companion object : EntityClass<Short, UserEntity>(UserTable)

    // Mapping properti ke kolom-kolom di UserTable
    var nama by UserTable.nama
    var alias by UserTable.alias
    var username by UserTable.username
    var password by UserTable.password
    var email by UserTable.email
    var emailVerifiedAt by UserTable.emailVerifiedAt
    var genderId by UserTable.genderId
    var photo by UserTable.photo
    var role by RoleEntity referencedOn UserTable.roleId
    var isActive by UserTable.isActive
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt

    // Relasi many-to-many dengan Unit
    var units by UnitEntity via UserUnitTable
}