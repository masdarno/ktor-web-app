package id.darno.module.wilayah.repository

import id.darno.core.pageddata.model.PagedResult
import id.darno.module.wilayah.domain.KelurahanDomain
import id.darno.module.wilayah.model.CreateKelurahanParams
import id.darno.module.wilayah.model.KelurahanListItem
import id.darno.module.wilayah.model.UpdateKelurahanParams

interface KelurahanRepository {

    suspend fun create(
        params: CreateKelurahanParams
    ): KelurahanDomain

    suspend fun findById(
        id: Int
    ): KelurahanDomain?

    suspend fun existsByKode(
        kode: String
    ): Boolean

    suspend fun update(
        id: Int,
        params: UpdateKelurahanParams
    ): KelurahanDomain

    suspend fun delete(
        id: Int
    ): Boolean

    suspend fun findAll(
        kecamatanId: Short,
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String
    ): PagedResult<KelurahanListItem>
}