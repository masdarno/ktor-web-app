package id.darno.module.wilayah.service

import id.darno.core.pageddata.model.PagedQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.wilayah.domain.KecamatanDomain
import id.darno.module.wilayah.model.CreateKecamatanParams
import id.darno.module.wilayah.model.KecamatanListItem
import id.darno.module.wilayah.model.UpdateKecamatanParams

interface KecamatanService {

    suspend fun create(
        params: CreateKecamatanParams
    ): KecamatanDomain

    suspend fun getById(
        id: Short
    ): KecamatanDomain

    suspend fun update(
        id: Short,
        params: UpdateKecamatanParams
    ): KecamatanDomain

    suspend fun delete(
        id: Short
    ): Boolean

    suspend fun getTable(
        query: PagedQuery,
        kabupatenId: Short?
    ): PagedResult<KecamatanListItem>

    suspend fun getAllActiveByKabupaten(
        kabupatenId: Short
    ): List<KecamatanDomain>
}