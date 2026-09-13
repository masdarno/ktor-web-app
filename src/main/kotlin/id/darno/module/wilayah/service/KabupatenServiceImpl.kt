package id.darno.module.wilayah.service

import id.darno.core.exceptions.service.ConflictException
import id.darno.core.exceptions.service.NotFoundException
import id.darno.core.pageddata.model.PagedQuery
import id.darno.module.wilayah.domain.KabupatenDomain
import id.darno.module.wilayah.model.CreateKabupatenParams
import id.darno.module.wilayah.model.UpdateKabupatenParams
import id.darno.module.wilayah.repository.KabupatenRepository
import id.darno.module.wilayah.repository.ProvinsiRepository

class KabupatenServiceImpl(
    private val kabupatenRepository: KabupatenRepository,
    private val provinsiRepository: ProvinsiRepository
) : KabupatenService {

    override suspend fun create(
        params: CreateKabupatenParams
    ): KabupatenDomain {

        provinsiRepository.findById(params.provinsiId)
            ?: throw NotFoundException(
                "Provinsi tidak ditemukan"
            )

        if (kabupatenRepository.existsByKode(
                params.provinsiId,
                params.kode
            )
        ) {
            throw ConflictException(
                "Kode kabupaten ${params.kode} sudah ada pada provinsi tersebut"
            )
        }

        return kabupatenRepository.create(params)
    }

    override suspend fun getById(
        id: Short
    ): KabupatenDomain {

        return kabupatenRepository.findById(id)
            ?: throw NotFoundException(
                "Kabupaten tidak ditemukan"
            )
    }

    override suspend fun update(
        id: Short,
        params: UpdateKabupatenParams
    ): KabupatenDomain {

        val existing = getById(id)

        val provinsiId =
            params.provinsiId ?: existing.provinsiId

        provinsiRepository.findById(provinsiId)
            ?: throw NotFoundException(
                "Provinsi tidak ditemukan"
            )

        params.kode?.let { kode ->

            val changed =
                kode != existing.kode ||
                        provinsiId != existing.provinsiId

            if (changed &&
                kabupatenRepository.existsByKode(
                    provinsiId,
                    kode
                )
            ) {
                throw ConflictException(
                    "Kode kabupaten $kode sudah ada pada provinsi tersebut"
                )
            }
        }

        return kabupatenRepository.update(id, params)
    }

    override suspend fun delete(
        id: Short
    ): Boolean {

        getById(id)

        return kabupatenRepository.delete(id)
    }

    override suspend fun getTable(
        query: PagedQuery,
        provinsiId: Short?
    ) = kabupatenRepository.findAll(
        provinsiId = provinsiId,
        search = query.search,
        page = query.page,
        pageSize = query.pageSize,
        sortBy = query.sortBy,
        sortDir = query.sortDir
    )

    override suspend fun getAllActiveByProvinsi(provinsiId: Short) =
        kabupatenRepository.findAllActiveByProvinsi(provinsiId)
}