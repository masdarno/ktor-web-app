package id.darno.module.wilayah.database.dao

import id.darno.module.user.database.dao.UserEntity
import id.darno.module.wilayah.database.table.ProvinsiTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class ProvinsiEntity(
    id: EntityID<Short>
) : Entity<Short>(id) {

    companion object :
        EntityClass<Short, ProvinsiEntity>(ProvinsiTable)

    var kode by ProvinsiTable.kode
    var nama by ProvinsiTable.nama
    var isActive by ProvinsiTable.isActive

    var createdAt by ProvinsiTable.createdAt
    var createdBy by UserEntity referencedOn ProvinsiTable.createdBy
    var updatedAt by ProvinsiTable.updatedAt
    var updatedBy by UserEntity optionalReferencedOn ProvinsiTable.updatedBy
}