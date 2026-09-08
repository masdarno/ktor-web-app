package id.darno.module.unit.database.table

import id.darno.core.database.TimeExpressions
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.datetime

object CompanyProfileTable : IdTable<Short>("company_profiles") {

    override val id: Column<EntityID<Short>> = short("id")
        .autoIncrement()
        .entityId()
    val namaPemerintah = varchar("nama_pemerintah", 150)
    val namaPerusahaan = varchar("nama_perusahaan", 150)
    val namaSingkat = varchar("nama_singkat", 100)
    val alamat = varchar("alamat", 500)
    val telepon = varchar("telepon", 50)
        .nullable()
    val email = varchar("email", 150)
        .nullable()
    val website = varchar("website", 150)
        .nullable()
    val logoKiri = varchar("logo_kiri", 255)
        .nullable()
    val logoKanan = varchar("logo_kanan", 255)
        .nullable()
    val createdAt = datetime("created_at")
        .defaultExpression(TimeExpressions.Companion.CurrentKotlinDateTime)
    val updatedAt = datetime("updated_at")
        .nullable()

    override val primaryKey = PrimaryKey(id)
}