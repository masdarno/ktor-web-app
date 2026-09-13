package id.darno.module.wilayah.service

import id.darno.core.exceptions.service.ConflictException
import id.darno.core.exceptions.service.NotFoundException
import id.darno.core.pageddata.model.PagedQuery
import id.darno.module.wilayah.domain.KelurahanDomain
import id.darno.module.wilayah.model.CreateKelurahanParams
import id.darno.module.wilayah.model.UpdateKelurahanParams
import id.darno.module.wilayah.repository.KecamatanRepository
import id.darno.module.wilayah.repository.KelurahanRepository

class KelurahanServiceImpl(
    private val kelurahanRepository: KelurahanRepository,
    private val kecamatanRepository: KecamatanRepository
) : KelurahanService {

    override suspend fun create(
        params: CreateKelurahanParams
    ): KelurahanDomain {

        kecamatanRepository.findById(params.kecamatanId)
            ?: throw NotFoundException(
                "Kecamatan tidak ditemukan"
            )

        if (kelurahanRepository.existsByKode(
                params.kecamatanId,
                params.kode
            )
        ) {
            throw ConflictException(
                "Kode kelurahan ${params.kode} sudah ada pada kecamatan tersebut"
            )
        }

        return kelurahanRepository.create(params)
    }

    override suspend fun getById(
        id: Int
    ): KelurahanDomain {

        return kelurahanRepository.findById(id)
            ?: throw NotFoundException(
                "Kelurahan tidak ditemukan"
            )
    }

    override suspend fun update(
        id: Int,
        params: UpdateKelurahanParams
    ): KelurahanDomain {

        val existing = getById(id)

        val kecamatanId =
            params.kecamatanId ?: existing.kecamatanId

        kecamatanRepository.findById(kecamatanId)
            ?: throw NotFoundException(
                "Kecamatan tidak ditemukan"
            )

        params.kode?.let { kode ->

            val changed =
                kode != existing.kode ||
                        kecamatanId != existing.kecamatanId

            if (changed &&
                kelurahanRepository.existsByKode(
                    kecamatanId,
                    kode
                )
            ) {
                throw ConflictException(
                    "Kode kelurahan $kode sudah ada pada kecamatan tersebut"
                )
            }
        }

        return kelurahanRepository.update(id, params)
    }

    override suspend fun delete(
        id: Int
    ): Boolean {

        getById(id)

        return kelurahanRepository.delete(id)
    }

    override suspend fun getTable(
        query: PagedQuery,
        kecamatanId: Short?
    ) = kelurahanRepository.findAll(
        kecamatanId = kecamatanId,
        search = query.search,
        page = query.page,
        pageSize = query.pageSize,
        sortBy = query.sortBy,
        sortDir = query.sortDir
    )
}