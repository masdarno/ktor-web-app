package id.darno.module.wilayah.service

import id.darno.core.pageddata.model.PagedQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.wilayah.domain.KelurahanDomain
import id.darno.module.wilayah.model.CreateKelurahanParams
import id.darno.module.wilayah.model.KelurahanListItem
import id.darno.module.wilayah.model.UpdateKelurahanParams

interface KelurahanService {

    suspend fun create(
        params: CreateKelurahanParams
    ): KelurahanDomain

    suspend fun getById(
        id: Int
    ): KelurahanDomain

    suspend fun update(
        id: Int,
        params: UpdateKelurahanParams
    ): KelurahanDomain

    suspend fun delete(
        id: Int
    ): Boolean

    suspend fun getTable(
        query: PagedQuery,
        kecamatanId: Short?
    ): PagedResult<KelurahanListItem>
}