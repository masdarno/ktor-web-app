package id.darno.module.wilayah.service

import id.darno.core.pageddata.model.PagedQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.wilayah.domain.ProvinsiDomain
import id.darno.module.wilayah.model.CreateProvinsiParams
import id.darno.module.wilayah.model.ProvinsiListItem
import id.darno.module.wilayah.model.UpdateProvinsiParams

interface ProvinsiService {

    suspend fun create(
        params: CreateProvinsiParams
    ): ProvinsiDomain

    suspend fun getById(
        id: Short
    ): ProvinsiDomain

    suspend fun update(
        id: Short,
        params: UpdateProvinsiParams
    ): ProvinsiDomain

    suspend fun delete(
        id: Short
    ): Boolean

    suspend fun getTable(
        query: PagedQuery
    ): PagedResult<ProvinsiListItem>

    suspend fun getAllActive(): List<ProvinsiDomain>
}