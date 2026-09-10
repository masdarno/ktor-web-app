package id.darno.module.unit.repository

import id.darno.core.database.query.DatabaseQuery
import id.darno.core.report.dto.shared.KopSuratReportDto
import id.darno.module.unit.database.table.CompanyProfileTable
import id.darno.module.unit.database.table.CompanyProfileTable.namaPemerintah
import org.jetbrains.exposed.v1.jdbc.select

class CompanyProfileRepositoryImpl(
    private val databaseQuery: DatabaseQuery
) : CompanyProfileRepository {
    override suspend fun find(): KopSuratReportDto = databaseQuery {
        CompanyProfileTable
            .select(CompanyProfileTable.namaPemerintah,
                CompanyProfileTable.namaPerusahaan,
                CompanyProfileTable.namaSingkat,
                CompanyProfileTable.alamat,
                CompanyProfileTable.logoKanan,
                CompanyProfileTable.logoKiri)
            .limit(1)
            .map {
                KopSuratReportDto(
                    namaPemerintah = it[namaPemerintah],
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