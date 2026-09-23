package id.darno.module.wilayah.repository

import id.darno.core.pageddata.model.PagedResult
import id.darno.module.wilayah.domain.KabupatenDomain
import id.darno.module.wilayah.model.CreateKabupatenParams
import id.darno.module.wilayah.model.KabupatenListItem
import id.darno.module.wilayah.model.UpdateKabupatenParams

interface KabupatenRepository {

    suspend fun create(
        params: CreateKabupatenParams
    ): KabupatenDomain

    suspend fun findById(
        id: Short
    ): KabupatenDomain?

    suspend fun existsByKode(
        kode: String
    ): Boolean

    suspend fun update(
        id: Short,
        params: UpdateKabupatenParams
    ): KabupatenDomain

    suspend fun delete(
        id: Short
    ): Boolean

    suspend fun findAll(
        provinsiId: Short?,
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String
    ): PagedResult<KabupatenListItem>

    suspend fun findAllActiveByProvinsi(
        provinsiId: Short
    ): List<KabupatenDomain>
}