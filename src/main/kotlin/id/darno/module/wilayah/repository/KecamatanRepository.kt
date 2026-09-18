package id.darno.module.wilayah.repository

import id.darno.core.pageddata.model.PagedResult
import id.darno.module.wilayah.domain.KecamatanDomain
import id.darno.module.wilayah.model.CreateKecamatanParams
import id.darno.module.wilayah.model.KecamatanListItem
import id.darno.module.wilayah.model.UpdateKecamatanParams

interface KecamatanRepository {

    suspend fun create(
        params: CreateKecamatanParams
    ): KecamatanDomain

    suspend fun findById(
        id: Short
    ): KecamatanDomain?

    suspend fun existsByKode(
        kode: String
    ): Boolean

    suspend fun update(
        id: Short,
        params: UpdateKecamatanParams
    ): KecamatanDomain

    suspend fun delete(
        id: Short
    ): Boolean

    suspend fun findAll(
        kabupatenId: Short?,
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String
    ): PagedResult<KecamatanListItem>

    suspend fun findAllActiveByKabupaten(
        kabupatenId: Short
    ): List<KecamatanDomain>
}