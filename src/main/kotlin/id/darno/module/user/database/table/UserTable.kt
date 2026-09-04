package id.darno.module.user.database.table

import id.darno.core.database.TimeExpressions
import id.darno.module.role.database.table.RoleTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.datetime

object UserTable : IdTable<Short>("users") {
    override val id: Column<EntityID<Short>> = short("id").autoIncrement().entityId()
    val name = varchar("name", 60)
    val alias = varchar("alias", 50).default("")
    val username = varchar("username", 10).uniqueIndex()
    val password = varchar("password", 100)
    val email = varchar("email", 50).uniqueIndex()
    val emailVerifiedAt = datetime("email_verified_at").nullable()
    val genderId = short("gender_id").references(GenderTable.id).default(2)
    val photo = varchar("photo", 100).default("face.jpg")
    val roleId = reference("role_id", RoleTable)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
        .defaultExpression(TimeExpressions.CurrentKotlinDateTime)
    val updatedAt = datetime("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}