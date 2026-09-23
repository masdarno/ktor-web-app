package id.darno.module.wilayah.service

import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.service.NotFoundException
import id.darno.core.pageddata.model.PagedQuery
import id.darno.module.wilayah.domain.ProvinsiDomain
import id.darno.module.wilayah.exception.ProvinsiException
import id.darno.module.wilayah.exception.toProvinsiException
import id.darno.module.wilayah.model.CreateProvinsiParams
import id.darno.module.wilayah.model.UpdateProvinsiParams
import id.darno.module.wilayah.repository.ProvinsiRepository

class ProvinsiServiceImpl(
    private val provinsiRepository: ProvinsiRepository
) : ProvinsiService {

    override suspend fun create(
        params: CreateProvinsiParams
    ): ProvinsiDomain {

        if (provinsiRepository.existsByKode(params.kode))
            throw ProvinsiException.KodeAlreadyExists(params.kode)

        if (provinsiRepository.existsByNama(params.nama))
            throw ProvinsiException.NamaAlreadyExists(params.nama)

        return try {
            provinsiRepository.create(params)
        }
        catch (e: DuplicateKeyException) {
            throw e.toProvinsiException(
                kode = params.kode,
                nama = params.nama
            )
        }
    }

    override suspend fun getById(
        id: Short
    ): ProvinsiDomain {

        return provinsiRepository.findById(id)
            ?: throw NotFoundException(
                "Provinsi tidak ditemukan"
            )
    }

    override suspend fun update(
        id: Short,
        params: UpdateProvinsiParams
    ): ProvinsiDomain {

        val existing = getById(id)

        params.kode?.let { kode ->
            if (kode != existing.kode) {
                if (provinsiRepository.existsByKode(kode))
                    throw ProvinsiException.KodeAlreadyExists(kode)
            }
        }

        params.nama?.let { nama ->
            if (nama != existing.nama) {
                if (provinsiRepository.existsByNama(nama))
                    throw ProvinsiException.NamaAlreadyExists(nama)
            }
        }

        return try {
            provinsiRepository.update(id, params)
        }
        catch (e: DuplicateKeyException) {
            throw e.toProvinsiException(
                kode = params.kode,
                nama = params.nama
            )
        }
    }

    override suspend fun delete(
        id: Short
    ): Boolean {

        getById(id)

        return try {
            provinsiRepository.delete(id)
        }
        catch (e: ForeignKeyException) {
            throw ProvinsiException.ProvinsiInUse(e)
        }
    }

    override suspend fun getTable(
        query: PagedQuery
    ) = provinsiRepository.findAll(
        search = query.search,
        page = query.page,
        pageSize = query.pageSize,
        sortBy = query.sortBy,
        sortDir = query.sortDir
    )

    override suspend fun getAllActive() = provinsiRepository.findAllActive()
}