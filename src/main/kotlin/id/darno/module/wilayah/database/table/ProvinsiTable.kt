package id.darno.module.wilayah.database.table

import id.darno.core.database.query.TimeExpressions
import id.darno.module.user.database.table.UserTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.datetime

object ProvinsiTable : IdTable<Short>("provinsi") {

    override val id: Column<EntityID<Short>> =
        short("id")
            .autoIncrement()
            .entityId()

    val kode = char("kode", 2).uniqueIndex()

    val nama = varchar("nama", 50).uniqueIndex()

    val isActive = bool("is_active").default(true)

    val createdAt = datetime("created_at")
        .defaultExpression(TimeExpressions.CurrentKotlinDateTime)

    val createdBy = reference("created_by", UserTable)

    val updatedAt = datetime("updated_at").nullable()

    val updatedBy = reference("updated_by", UserTable).nullable()

    override val primaryKey = PrimaryKey(id)
}