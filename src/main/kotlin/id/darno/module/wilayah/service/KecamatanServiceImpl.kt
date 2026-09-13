package id.darno.module.wilayah.service

import id.darno.core.exceptions.service.ConflictException
import id.darno.core.exceptions.service.NotFoundException
import id.darno.core.pageddata.model.PagedQuery
import id.darno.module.wilayah.domain.KecamatanDomain
import id.darno.module.wilayah.model.CreateKecamatanParams
import id.darno.module.wilayah.model.UpdateKecamatanParams
import id.darno.module.wilayah.repository.KabupatenRepository
import id.darno.module.wilayah.repository.KecamatanRepository

class KecamatanServiceImpl(
    private val kecamatanRepository: KecamatanRepository,
    private val kabupatenRepository: KabupatenRepository
) : KecamatanService {

    override suspend fun create(
        params: CreateKecamatanParams
    ): KecamatanDomain {

        kabupatenRepository.findById(params.kabupatenId)
            ?: throw NotFoundException(
                "Kabupaten tidak ditemukan"
            )

        if (kecamatanRepository.existsByKode(
                params.kabupatenId,
                params.kode
            )
        ) {
            throw ConflictException(
                "Kode kecamatan ${params.kode} sudah ada pada kabupaten tersebut"
            )
        }

        return kecamatanRepository.create(params)
    }

    override suspend fun getById(
        id: Short
    ): KecamatanDomain {

        return kecamatanRepository.findById(id)
            ?: throw NotFoundException(
                "Kecamatan tidak ditemukan"
            )
    }

    override suspend fun update(
        id: Short,
        params: UpdateKecamatanParams
    ): KecamatanDomain {

        val existing = getById(id)

        val kabupatenId =
            params.kabupatenId ?: existing.kabupatenId

        kabupatenRepository.findById(kabupatenId)
            ?: throw NotFoundException(
                "Kabupaten tidak ditemukan"
            )

        params.kode?.let { kode ->

            val changed =
                kode != existing.kode ||
                        kabupatenId != existing.kabupatenId

            if (changed &&
                kecamatanRepository.existsByKode(
                    kabupatenId,
                    kode
                )
            ) {
                throw ConflictException(
                    "Kode kecamatan $kode sudah ada pada kabupaten tersebut"
                )
            }
        }

        return kecamatanRepository.update(id, params)
    }

    override suspend fun delete(
        id: Short
    ): Boolean {

        getById(id)

        return kecamatanRepository.delete(id)
    }

    override suspend fun getTable(
        query: PagedQuery,
        kabupatenId: Short?
    ) = kecamatanRepository.findAll(
        kabupatenId = kabupatenId,
        search = query.search,
        page = query.page,
        pageSize = query.pageSize,
        sortBy = query.sortBy,
        sortDir = query.sortDir
    )

    override suspend fun getAllActiveByKabupaten(kabupatenId: Short) =
        kecamatanRepository.findAllActiveByKabupaten(kabupatenId)
}