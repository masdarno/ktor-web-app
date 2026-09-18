package id.darno.module.wilayah.service

import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.service.NotFoundException
import id.darno.core.pageddata.model.PagedQuery
import id.darno.module.wilayah.domain.KelurahanDomain
import id.darno.module.wilayah.exception.KelurahanException
import id.darno.module.wilayah.exception.toKelurahanException
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

        params.kecamatanId.let {
            try {
                kecamatanRepository.findById(it)
            }
            catch (e: NotFoundException) {
                throw KelurahanException.KecamatanNotFound(it)
            }
        }

        if (kelurahanRepository.existsByKode(params.kode))
            throw KelurahanException.KodeAlreadyExists(params.kode)

        return try {
            kelurahanRepository.create(params)
        }
        catch (e: DuplicateKeyException) {
            throw e.toKelurahanException(
                kode = params.kode
            )
        }
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

        params.kecamatanId?.let { kecamatanId ->
            if (kecamatanId != existing.kecamatanId){
                if (kecamatanRepository.findById(kecamatanId) == null)
                    throw KelurahanException.KecamatanNotFound(kecamatanId)
            }
        }

        params.kode?.let { kode ->
            if (kode != existing.kode){
                if (kelurahanRepository.existsByKode(kode))
                    throw KelurahanException.KodeAlreadyExists(kode)
            }

        }

        return try {
            kelurahanRepository.update(id, params)
        }
        catch (e: DuplicateKeyException) {
            throw e.toKelurahanException(
                kode = params.kode
            )
        }
    }

    override suspend fun delete(
        id: Int
    ): Boolean {

        getById(id)

        return try {
            kelurahanRepository.delete(id)
        }
        catch (e: ForeignKeyException) {
            throw KelurahanException.KelurahanInUse(e)
        }
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