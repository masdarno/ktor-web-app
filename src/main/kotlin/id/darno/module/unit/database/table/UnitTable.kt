package id.darno.module.unit.database.table

import id.darno.core.database.query.TimeExpressions
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.datetime

object UnitTable : IdTable<Short>("units") {
    override val id: Column<EntityID<Short>> = short("id").autoIncrement().entityId()
    val nama = varchar("nama", 50).uniqueIndex()
    val isActive = bool("is_active").default(true)
    val companyProfileId = short("company_profile_id")
        .references(CompanyProfileTable.id)
        .default(1)
    val createdAt = datetime("created_at")
        .defaultExpression(TimeExpressions.CurrentKotlinDateTime)
    val updatedAt = datetime("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}