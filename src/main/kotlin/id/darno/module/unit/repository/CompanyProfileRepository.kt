package id.darno.module.unit.repository

import id.darno.core.report.KopSurat

interface CompanyProfileRepository {
    suspend fun find(): KopSurat
}