package id.darno.module.auth.database.table

import id.darno.module.unit.database.table.UnitTable
import id.darno.module.user.database.table.UserTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object RememberMeTokenTable : Table("remember_me_tokens") {
    val selector = varchar("selector", 64)
    val userId = reference("user_id", UserTable)
    val unitId = reference("unit_id", UnitTable)
    val validatorHash = varchar("validator_hash", 255)
    val expiresAt = datetime("expires_at")

    override val primaryKey = PrimaryKey(selector)
}