package id.darno.module.wilayah.repository

import id.darno.core.database.query.DatabaseQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.user.database.dao.UserEntity
import id.darno.module.wilayah.database.dao.KecamatanEntity
import id.darno.module.wilayah.database.dao.KelurahanEntity
import id.darno.module.wilayah.database.table.KecamatanTable
import id.darno.module.wilayah.database.table.KelurahanTable
import id.darno.module.wilayah.domain.KelurahanDomain
import id.darno.module.wilayah.mapper.toKelurahanDomain
import id.darno.module.wilayah.model.CreateKelurahanParams
import id.darno.module.wilayah.model.KelurahanListItem
import id.darno.module.wilayah.model.UpdateKelurahanParams
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll

class KelurahanRepositoryImpl(
    private val databaseQuery: DatabaseQuery
) : KelurahanRepository {

    override suspend fun create(
        params: CreateKelurahanParams
    ): KelurahanDomain = databaseQuery {

        KelurahanEntity.new {
            kecamatan = KecamatanEntity[params.kecamatanId]
            kode = params.kode
            nama = params.nama
            isActive = params.isActive
            createdBy = UserEntity[params.createdBy]
        }.toKelurahanDomain()
    }

    override suspend fun findById(
        id: Int
    ): KelurahanDomain? = databaseQuery {

        KelurahanEntity.findById(id)
            ?.toKelurahanDomain()
    }

    override suspend fun existsByKode(
        kode: String
    ): Boolean = databaseQuery {

        KelurahanEntity
            .find {
                (KelurahanTable.kode eq kode)
            }
            .any()
    }

    override suspend fun update(
        id: Int,
        params: UpdateKelurahanParams
    ): KelurahanDomain = databaseQuery {

        val entity = KelurahanEntity[id]

        entity.apply {

            params.kecamatanId?.let {
                kecamatan = KecamatanEntity[it]
            }

            kode = params.kode ?: kode
            nama = params.nama ?: nama
            isActive = params.isActive ?: isActive
            updatedBy = UserEntity[params.updatedBy]

        }.toKelurahanDomain()
    }

    override suspend fun delete(
        id: Int
    ): Boolean = databaseQuery {

        KelurahanTable.deleteWhere {
            KelurahanTable.id eq id
        }

        true
    }

    override suspend fun findAll(
        kecamatanId: Short?,
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String
    ): PagedResult<KelurahanListItem> = databaseQuery {

        val sortColumn = when (sortBy) {
            "kode" -> KelurahanTable.kode
            "nama" -> KelurahanTable.nama
            "kecamatan" -> KecamatanTable.nama
            "active" -> KelurahanTable.isActive
            else -> KelurahanTable.id
        }

        val order = when (sortDir.lowercase()) {
            "asc" -> SortOrder.ASC
            else -> SortOrder.DESC
        }

        val searchFilter = search
            ?.takeIf { it.isNotBlank() }
            ?.let {
                (KelurahanTable.kode like "%$it%") or
                        (KelurahanTable.nama like "%$it%")
            }

        val parentFilter = kecamatanId?.let {
            KelurahanTable.kecamatanId eq it
        }

        val filter = listOfNotNull(
            searchFilter,
            parentFilter
        ).reduceOrNull { acc, condition ->
            acc and condition
        }

        val baseQuery = KelurahanTable
            .innerJoin(KecamatanTable)
            .selectAll()
            .let {
                if (filter != null) {
                    it.where { filter }
                } else {
                    it
                }
            }

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
                KelurahanListItem(
                    id = it[KelurahanTable.id].value,
                    kecamatanId = it[KecamatanTable.id].value,
                    kecamatanNama = it[KecamatanTable.nama],
                    kode = it[KelurahanTable.kode],
                    nama = it[KelurahanTable.nama],
                    isActive = it[KelurahanTable.isActive]
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
}