package id.darno.module.wilayah.service

import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.service.NotFoundException
import id.darno.core.pageddata.model.PagedQuery
import id.darno.module.wilayah.domain.KabupatenDomain
import id.darno.module.wilayah.exception.KabupatenException
import id.darno.module.wilayah.exception.toKabupatenException
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

        params.provinsiId.let{
            try {
                provinsiRepository.findById(it)
            }
            catch (e: NotFoundException) {
                throw KabupatenException.ProvinsiNotFound(it)
            }
        }

        if (kabupatenRepository.existsByKode(params.kode))
            throw KabupatenException.KodeAlreadyExists(params.kode)

        return try {
            kabupatenRepository.create(params)
        }
        catch (e: DuplicateKeyException) {
            throw e.toKabupatenException(
                kode = params.kode
            )
        }
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

        params.provinsiId?.let { provinsiId ->
            if (provinsiId != existing.provinsiId){
                if (provinsiRepository.findById(provinsiId) == null)
                    throw KabupatenException.ProvinsiNotFound(provinsiId)
            }
        }

        params.kode?.let { kode ->
            if (kode != existing.kode){
                if (kabupatenRepository.existsByKode(kode))
                    throw KabupatenException.KodeAlreadyExists(kode)
            }

        }

        return try {
            kabupatenRepository.update(id, params)
        }
        catch (e: DuplicateKeyException) {
            throw e.toKabupatenException(
                kode = params.kode
            )
        }
    }

    override suspend fun delete(
        id: Short
    ): Boolean {

        getById(id)

        return try {
            kabupatenRepository.delete(id)
        }
        catch (e: ForeignKeyException) {
            throw KabupatenException.KabupatenInUse(e)
        }
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