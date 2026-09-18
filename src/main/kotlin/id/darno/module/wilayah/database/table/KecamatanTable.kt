package id.darno.module.wilayah.database.table

import id.darno.core.database.query.TimeExpressions
import id.darno.module.user.database.table.UserTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.datetime

object KecamatanTable : IdTable<Short>("kecamatan") {

    const val KODE_UNIQUE_CONSTRAINT = "uq_kecamatan_kode"
    const val KABUPATEN_FK_CONSTRAINT = "fk_kecamatan_kabupaten_id"

    override val id: Column<EntityID<Short>> =
        short("id")
            .autoIncrement()
            .entityId()

    val kabupatenId =
        reference(
            "kabupaten_id",
            KabupatenTable,
            fkName = KABUPATEN_FK_CONSTRAINT
        )

    val kode =
        char("kode", 6)
            .uniqueIndex(KODE_UNIQUE_CONSTRAINT)

    val nama =
        varchar("nama", 50).default("")

    val isActive =
        bool("is_active").default(true)

    val createdAt =
        datetime("created_at")
            .defaultExpression(TimeExpressions.CurrentKotlinDateTime)

    val createdBy =
        reference("created_by", UserTable)

    val updatedAt =
        datetime("updated_at").nullable()

    val updatedBy =
        reference("updated_by", UserTable).nullable()

    init {
        uniqueIndex(kabupatenId, kode)
    }

    override val primaryKey = PrimaryKey(id)
}