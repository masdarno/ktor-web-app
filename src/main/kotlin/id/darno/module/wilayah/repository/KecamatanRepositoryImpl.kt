package id.darno.module.wilayah.repository

import id.darno.core.database.query.DatabaseQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.user.database.dao.UserEntity
import id.darno.module.wilayah.database.dao.KabupatenEntity
import id.darno.module.wilayah.database.dao.KecamatanEntity
import id.darno.module.wilayah.database.table.KabupatenTable
import id.darno.module.wilayah.database.table.KecamatanTable
import id.darno.module.wilayah.domain.KecamatanDomain
import id.darno.module.wilayah.mapper.toKecamatanDomain
import id.darno.module.wilayah.model.CreateKecamatanParams
import id.darno.module.wilayah.model.KecamatanListItem
import id.darno.module.wilayah.model.UpdateKecamatanParams
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll

class KecamatanRepositoryImpl(
    private val databaseQuery: DatabaseQuery
) : KecamatanRepository {

    override suspend fun create(
        params: CreateKecamatanParams
    ): KecamatanDomain = databaseQuery {

        KecamatanEntity.new {
            kabupaten = KabupatenEntity[params.kabupatenId]
            kode = params.kode
            nama = params.nama
            isActive = params.isActive
            createdBy = UserEntity[params.createdBy]
        }.toKecamatanDomain()
    }

    override suspend fun findById(
        id: Short
    ): KecamatanDomain? = databaseQuery {

        KecamatanEntity.findById(id)
            ?.toKecamatanDomain()
    }

    override suspend fun existsByKode(
        kode: String
    ): Boolean = databaseQuery {

        KecamatanEntity
            .find {
                (KecamatanTable.kode eq kode)
            }
            .any()
    }

    override suspend fun update(
        id: Short,
        params: UpdateKecamatanParams
    ): KecamatanDomain = databaseQuery {

        val entity = KecamatanEntity[id]

        entity.apply {

            params.kabupatenId?.let {
                kabupaten = KabupatenEntity[it]
            }

            kode = params.kode ?: kode
            nama = params.nama ?: nama
            isActive = params.isActive ?: isActive
            updatedBy = UserEntity[params.updatedBy]

        }.toKecamatanDomain()
    }

    override suspend fun delete(
        id: Short
    ): Boolean = databaseQuery {

        KecamatanTable.deleteWhere {
            KecamatanTable.id eq id
        }

        true
    }

    override suspend fun findAll(
        kabupatenId: Short,
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String
    ): PagedResult<KecamatanListItem> = databaseQuery {

        val sortColumn = when (sortBy) {
            "kode" -> KecamatanTable.kode
            "nama" -> KecamatanTable.nama
            "kabupaten" -> KabupatenTable.nama
            "active" -> KecamatanTable.isActive
            else -> KecamatanTable.id
        }

        val order = when (sortDir.lowercase()) {
            "asc" -> SortOrder.ASC
            else -> SortOrder.DESC
        }

        val searchFilter = search
            ?.takeIf { it.isNotBlank() }
            ?.let {
                (KecamatanTable.kode like "%$it%") or
                        (KecamatanTable.nama like "%$it%")
            }

        val parentFilter =
            KecamatanTable.kabupatenId eq kabupatenId

        val filter = searchFilter?.let {
            it and parentFilter
        } ?: parentFilter

        val baseQuery = KecamatanTable
            .innerJoin(KabupatenTable)
            .selectAll()
            .where { filter }

        val total = baseQuery.count()

        val totalPages =
            if (total == 0L) {
                1
            } else {
                ((total + pageSize - 1) / pageSize).toInt()
            }

        val offset = (page - 1) * pageSize

        val data = baseQuery
            .orderBy(sortColumn to order)
            .limit(pageSize)
            .offset(offset.toLong())
            .map {
                KecamatanListItem(
                    id = it[KecamatanTable.id].value,
                    kabupatenId = it[KabupatenTable.id].value,
                    kabupatenNama = it[KabupatenTable.nama],
                    kode = it[KecamatanTable.kode],
                    nama = it[KecamatanTable.nama],
                    isActive = it[KecamatanTable.isActive]
                )
            }

        PagedResult(
            data = data,
            page = page,
            pageSize = pageSize,
            total = total,
            totalPages = totalPages
        )
    }

    override suspend fun findAllActiveByKabupaten(
        kabupatenId: Short
    ): List<KecamatanDomain> = databaseQuery {
        KecamatanEntity
            .find {
                (KecamatanTable.kabupatenId eq kabupatenId) and
                        (KecamatanTable.isActive eq true)
            }
            .orderBy(KecamatanTable.nama to SortOrder.ASC)
            .map { it.toKecamatanDomain() }
    }
}