package id.darno.module.wilayah.service

import id.darno.core.exceptions.service.ConflictException
import id.darno.core.exceptions.service.NotFoundException
import id.darno.core.pageddata.model.PagedQuery
import id.darno.module.wilayah.domain.ProvinsiDomain
import id.darno.module.wilayah.model.CreateProvinsiParams
import id.darno.module.wilayah.model.UpdateProvinsiParams
import id.darno.module.wilayah.repository.ProvinsiRepository

class ProvinsiServiceImpl(
    private val provinsiRepository: ProvinsiRepository
) : ProvinsiService {

    override suspend fun create(
        params: CreateProvinsiParams
    ): ProvinsiDomain {

        if (provinsiRepository.existsByKode(params.kode)) {
            throw ConflictException(
                "Kode provinsi ${params.kode} sudah ada"
            )
        }

        if (provinsiRepository.existsByNama(params.nama)) {
            throw ConflictException(
                "Nama provinsi ${params.nama} sudah ada"
            )
        }

        return provinsiRepository.create(params)
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
            if (kode != existing.kode &&
                provinsiRepository.existsByKode(kode)
            ) {
                throw ConflictException(
                    "Kode provinsi $kode sudah ada"
                )
            }
        }

        params.nama?.let { nama ->
            if (nama != existing.nama &&
                provinsiRepository.existsByNama(nama)
            ) {
                throw ConflictException(
                    "Nama provinsi $nama sudah ada"
                )
            }
        }

        return provinsiRepository.update(id, params)
    }

    override suspend fun delete(
        id: Short
    ): Boolean {

        getById(id)

        return provinsiRepository.delete(id)
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