package id.darno.module.auth.database.table

import id.darno.module.user.database.table.UserTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object PasswordResetTokenTable : Table("password_reset_tokens") {
    val token = varchar("token", 64)
    val userId = reference("user_id", UserTable)
    val expiresAt = datetime("expires_at")
    val usedAt = datetime("used_at").nullable()

    override val primaryKey = PrimaryKey(token)
}
