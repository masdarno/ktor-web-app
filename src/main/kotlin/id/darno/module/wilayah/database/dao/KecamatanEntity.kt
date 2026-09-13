package id.darno.module.wilayah.database.dao

import id.darno.module.user.database.dao.UserEntity
import id.darno.module.wilayah.database.table.KecamatanTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class KecamatanEntity(
    id: EntityID<Short>
) : Entity<Short>(id) {

    companion object : EntityClass<Short, KecamatanEntity>(KecamatanTable)

    var kabupaten by KabupatenEntity referencedOn KecamatanTable.kabupatenId

    var kode by KecamatanTable.kode
    var nama by KecamatanTable.nama
    var isActive by KecamatanTable.isActive
    var createdAt by KecamatanTable.createdAt
    var createdBy by UserEntity referencedOn KecamatanTable.createdBy
    var updatedAt by KecamatanTable.updatedAt
    var updatedBy by UserEntity optionalReferencedOn KecamatanTable.updatedBy
}