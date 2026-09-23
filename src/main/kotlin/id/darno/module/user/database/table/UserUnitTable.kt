package id.darno.module.user.database.table

import id.darno.module.unit.database.table.UnitTable
import org.jetbrains.exposed.v1.core.Table

object UserUnitTable : Table("user_units") {
    val userId = reference("user_id", UserTable)
    val unitId = reference("unit_id", UnitTable)

    override val primaryKey = PrimaryKey(userId, unitId)
}