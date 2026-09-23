package id.darno.module.auth.database.table

import id.darno.module.user.database.table.UserTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object EmailVerificationTokenTable : Table("email_verification_tokens") {
    val token = varchar("token", 255)
    val userId = reference("user_id", UserTable)
    val expiresAt = datetime("expires_at")

    override val primaryKey = PrimaryKey(token)
}