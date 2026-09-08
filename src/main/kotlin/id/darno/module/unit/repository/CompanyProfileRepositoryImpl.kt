package id.darno.module.unit.repository

import id.darno.core.database.dbQuery
import id.darno.core.report.KopSurat
import id.darno.module.unit.database.dao.UnitEntity
import id.darno.module.unit.database.table.CompanyProfileTable
import id.darno.module.unit.database.table.CompanyProfileTable.namaPemerintah
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.unit.mapper.toUnitDomain
import org.jetbrains.exposed.v1.jdbc.select

class CompanyProfileRepositoryImpl: CompanyProfileRepository {
    override suspend fun find(): KopSurat = dbQuery {
        CompanyProfileTable
            .select(CompanyProfileTable.namaPemerintah,
                CompanyProfileTable.namaPerusahaan,
                CompanyProfileTable.namaSingkat,
                CompanyProfileTable.alamat,
                CompanyProfileTable.logoKanan,
                CompanyProfileTable.logoKiri)
            .limit(1)
            .map {
                KopSurat(
                    namaPemerintah = it[CompanyProfileTable.namaPemerintah],
                    namaPerusahaan = it[CompanyProfileTable.namaPerusahaan],
                    namaSingkat = it[CompanyProfileTable.namaSingkat],
                    alamat = it[CompanyProfileTable.alamat],
                    logoKanan = it[CompanyProfileTable.logoKanan] ?: "",
                    logoKiri = it[CompanyProfileTable.logoKiri] ?: ""
                )
            }
            .single()
    }
}