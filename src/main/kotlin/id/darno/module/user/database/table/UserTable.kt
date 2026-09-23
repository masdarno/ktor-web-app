package id.darno.module.user.database.table

import id.darno.core.database.query.TimeExpressions
import id.darno.module.role.database.table.RoleTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.datetime

object UserTable : IdTable<Short>("users") {

    const val USERNAME_UNIQUE_CONSTRAINT = "uq_users_username"
    const val EMAIL_UNIQUE_CONSTRAINT = "uq_users_email"
    const val ROLE_FK_CONSTRAINT = "fk_users_role_id"
    const val GENDER_FK_CONSTRAINT = "fk_users_gender_id"

    override val id: Column<EntityID<Short>> = short("id").autoIncrement().entityId()
    val nama = varchar("nama", 60)
    val alias = varchar("alias", 50).default("")
    val username = varchar("username", 10).uniqueIndex(USERNAME_UNIQUE_CONSTRAINT)
    val password = varchar("password", 100)
    val email = varchar("email", 50).uniqueIndex(EMAIL_UNIQUE_CONSTRAINT)
    val emailVerifiedAt = datetime("email_verified_at").nullable()
    val genderId = short("gender_id")
        .references(
            ref = GenderTable.id,
            onUpdate = ReferenceOption.CASCADE,
            fkName = GENDER_FK_CONSTRAINT
        )
        .default(2)
    val photo = varchar("photo", 100).default("male.jpg")
    val roleId = reference(
        name = "role_id",
        refColumn = RoleTable.id,
        fkName = ROLE_FK_CONSTRAINT,
        onUpdate = ReferenceOption.CASCADE
    )
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
        .defaultExpression(TimeExpressions.CurrentKotlinDateTime)
    val updatedAt = datetime("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}