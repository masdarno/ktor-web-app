package id.darno.module.unit.repository

import id.darno.core.report.dto.shared.KopSuratReportDto

interface CompanyProfileRepository {
    suspend fun find(): KopSuratReportDto
}