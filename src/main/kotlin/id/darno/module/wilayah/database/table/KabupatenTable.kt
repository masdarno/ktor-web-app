package id.darno.module.wilayah.database.table

import id.darno.module.user.database.table.UserTable
import id.darno.core.database.query.TimeExpressions
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.datetime

object KabupatenTable : IdTable<Short>("kabupaten") {

    override val id: Column<EntityID<Short>> =
        short("id")
            .autoIncrement()
            .entityId()

    val provinsiId =
        reference("provinsi_id", ProvinsiTable)

    val kode =
        char("kode", 4)

    val nama =
        varchar("nama", 50).default("")

    val isActive =
        bool("is_active").default(true)

    val createdAt =
        datetime("created_at")
            .defaultExpression(
                TimeExpressions.CurrentKotlinDateTime
            )

    val createdBy =
        reference("created_by", UserTable)

    val updatedAt =
        datetime("updated_at").nullable()

    val updatedBy =
        reference("updated_by", UserTable).nullable()

    init {
        uniqueIndex(provinsiId, kode)
    }

    override val primaryKey = PrimaryKey(id)
}