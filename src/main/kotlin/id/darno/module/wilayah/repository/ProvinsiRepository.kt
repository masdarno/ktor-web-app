package id.darno.module.wilayah.repository

import id.darno.core.pageddata.model.PagedResult
import id.darno.module.wilayah.domain.ProvinsiDomain
import id.darno.module.wilayah.model.CreateProvinsiParams
import id.darno.module.wilayah.model.ProvinsiListItem
import id.darno.module.wilayah.model.UpdateProvinsiParams

interface ProvinsiRepository {

    suspend fun create(
        params: CreateProvinsiParams
    ): ProvinsiDomain

    suspend fun findById(
        id: Short
    ): ProvinsiDomain?

    suspend fun findByKode(
        kode: String
    ): ProvinsiDomain?

    suspend fun existsByKode(
        kode: String
    ): Boolean

    suspend fun existsByNama(
        nama: String
    ): Boolean

    suspend fun update(
        id: Short,
        params: UpdateProvinsiParams
    ): ProvinsiDomain

    suspend fun delete(
        id: Short
    ): Boolean

    suspend fun findAll(
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String
    ): PagedResult<ProvinsiListItem>

    suspend fun findAllActive(): List<ProvinsiDomain>
}