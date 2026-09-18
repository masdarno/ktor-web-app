package id.darno.module.wilayah.service

import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.service.NotFoundException
import id.darno.core.pageddata.model.PagedQuery
import id.darno.module.wilayah.domain.KecamatanDomain
import id.darno.module.wilayah.exception.KecamatanException
import id.darno.module.wilayah.exception.toKecamatanException
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

        params.kabupatenId.let {
            try {
                kabupatenRepository.findById(it)
            }
            catch (e: NotFoundException) {
                throw KecamatanException.KabupatenNotFound(it)
            }
        }

        if (kecamatanRepository.existsByKode(params.kode))
            throw KecamatanException.KodeAlreadyExists(params.kode)

        return try {
            kecamatanRepository.create(params)
        }
        catch (e: DuplicateKeyException) {
            throw e.toKecamatanException(
                kode = params.kode
            )
        }
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

        params.kabupatenId?.let { kabupatenId ->
            if (kabupatenId != existing.kabupatenId){
                if (kabupatenRepository.findById(kabupatenId) == null)
                    throw KecamatanException.KabupatenNotFound(kabupatenId)
            }
        }

        params.kode?.let { kode ->
            if (kode != existing.kode){
                if (kecamatanRepository.existsByKode(kode))
                    throw KecamatanException.KodeAlreadyExists(kode)
            }

        }

        return try {
            kecamatanRepository.update(id, params)
        }
        catch (e: DuplicateKeyException) {
            throw e.toKecamatanException(
                kode = params.kode
            )
        }
    }

    override suspend fun delete(
        id: Short
    ): Boolean {

        getById(id)

        return try {
            kecamatanRepository.delete(id)
        }
        catch (e: ForeignKeyException) {
            throw KecamatanException.KecamatanInUse(e)
        }
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