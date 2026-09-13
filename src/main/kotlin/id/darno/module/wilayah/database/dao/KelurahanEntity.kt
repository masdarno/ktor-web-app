package id.darno.module.wilayah.database.dao

import id.darno.module.user.database.dao.UserEntity
import id.darno.module.wilayah.database.table.KelurahanTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class KelurahanEntity(
    id: EntityID<Int>
) : Entity<Int>(id) {

    companion object : EntityClass<Int, KelurahanEntity>(KelurahanTable)

    var kecamatan by KecamatanEntity referencedOn KelurahanTable.kecamatanId

    var kode by KelurahanTable.kode
    var nama by KelurahanTable.nama
    var isActive by KelurahanTable.isActive
    var createdAt by KelurahanTable.createdAt
    var createdBy by UserEntity referencedOn KelurahanTable.createdBy
    var updatedAt by KelurahanTable.updatedAt
    var updatedBy by UserEntity optionalReferencedOn KelurahanTable.updatedBy
}