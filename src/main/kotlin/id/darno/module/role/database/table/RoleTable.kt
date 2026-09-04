package id.darno.module.role.database.table

import id.darno.core.database.TimeExpressions
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.datetime

object RoleTable : IdTable<Short>("roles") {
    override val id: Column<EntityID<Short>> = short("id").autoIncrement().entityId()
    val name = varchar("name", 50).uniqueIndex()
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
        .defaultExpression(TimeExpressions.Companion.CurrentKotlinDateTime)
    val updatedAt = datetime("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}