package id.darno.module.wilayah.database.dao

import id.darno.module.user.database.dao.UserEntity
import id.darno.module.wilayah.database.table.KabupatenTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class KabupatenEntity(
    id: EntityID<Short>
) : Entity<Short>(id) {

    companion object : EntityClass<Short, KabupatenEntity>(KabupatenTable)

    var provinsi by ProvinsiEntity referencedOn KabupatenTable.provinsiId

    var kode by KabupatenTable.kode
    var nama by KabupatenTable.nama
    var isActive by KabupatenTable.isActive
    var createdAt by KabupatenTable.createdAt
    var createdBy by UserEntity referencedOn KabupatenTable.createdBy
    var updatedAt by KabupatenTable.updatedAt
    var updatedBy by UserEntity optionalReferencedOn KabupatenTable.updatedBy
}