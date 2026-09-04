package id.darno.module.user.database.table

import id.darno.core.database.TimeExpressions
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.timestamp

object GenderTable : Table("genders") {
    val id = short("id").autoIncrement()
    val nama = varchar("nama", 50).uniqueIndex()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
        .defaultExpression(TimeExpressions.CurrentKotlinDateTime)
    val updatedAt = timestamp("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}