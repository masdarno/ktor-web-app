package id.darno.module.wilayah.service

import id.darno.core.pageddata.model.PagedQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.wilayah.domain.KabupatenDomain
import id.darno.module.wilayah.model.CreateKabupatenParams
import id.darno.module.wilayah.model.KabupatenListItem
import id.darno.module.wilayah.model.UpdateKabupatenParams

interface KabupatenService {

    suspend fun create(
        params: CreateKabupatenParams
    ): KabupatenDomain

    suspend fun getById(
        id: Short
    ): KabupatenDomain

    suspend fun update(
        id: Short,
        params: UpdateKabupatenParams
    ): KabupatenDomain

    suspend fun delete(
        id: Short
    ): Boolean

    suspend fun getTable(
        query: PagedQuery,
        provinsiId: Short?
    ): PagedResult<KabupatenListItem>

    suspend fun getAllActiveByProvinsi(
        provinsiId: Short
    ): List<KabupatenDomain>
}