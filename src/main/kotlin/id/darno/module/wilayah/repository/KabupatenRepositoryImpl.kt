package id.darno.module.wilayah.repository

import id.darno.core.database.query.DatabaseQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.user.database.dao.UserEntity
import id.darno.module.wilayah.database.dao.KabupatenEntity
import id.darno.module.wilayah.database.dao.ProvinsiEntity
import id.darno.module.wilayah.database.table.KabupatenTable
import id.darno.module.wilayah.database.table.ProvinsiTable
import id.darno.module.wilayah.domain.KabupatenDomain
import id.darno.module.wilayah.mapper.toKabupatenDomain
import id.darno.module.wilayah.model.CreateKabupatenParams
import id.darno.module.wilayah.model.KabupatenListItem
import id.darno.module.wilayah.model.UpdateKabupatenParams
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll

class KabupatenRepositoryImpl(
    private val databaseQuery: DatabaseQuery
) : KabupatenRepository {

    override suspend fun create(
        params: CreateKabupatenParams
    ): KabupatenDomain = databaseQuery {

        KabupatenEntity.new {
            provinsi = ProvinsiEntity[params.provinsiId]
            kode = params.kode
            nama = params.nama
            isActive = params.isActive
            createdBy = UserEntity[params.createdBy]
        }.toKabupatenDomain()
    }

    override suspend fun findById(
        id: Short
    ): KabupatenDomain? = databaseQuery {

        KabupatenEntity.findById(id)
            ?.toKabupatenDomain()
    }

    override suspend fun existsByKode(
        provinsiId: Short,
        kode: String
    ): Boolean = databaseQuery {

        KabupatenEntity
            .find {
                (KabupatenTable.provinsiId eq provinsiId) and
                        (KabupatenTable.kode eq kode)
            }
            .any()
    }

    override suspend fun update(
        id: Short,
        params: UpdateKabupatenParams
    ): KabupatenDomain = databaseQuery {

        val entity = KabupatenEntity[id]

        entity.apply {

            params.provinsiId?.let {
                provinsi = ProvinsiEntity[it]
            }

            kode = params.kode ?: kode
            nama = params.nama ?: nama
            isActive = params.isActive ?: isActive
            updatedBy = UserEntity[params.updatedBy]

        }.toKabupatenDomain()
    }

    override suspend fun delete(
        id: Short
    ): Boolean = databaseQuery {

        KabupatenTable.deleteWhere {
            KabupatenTable.id eq id
        }

        true
    }

    override suspend fun findAll(
        provinsiId: Short?,
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String
    ): PagedResult<KabupatenListItem> = databaseQuery {

        val sortColumn = when (sortBy) {
            "kode" -> KabupatenTable.kode
            "nama" -> KabupatenTable.nama
            "provinsi" -> ProvinsiTable.nama
            "active" -> KabupatenTable.isActive
            else -> KabupatenTable.id
        }

        val order = when (sortDir.lowercase()) {
            "asc" -> SortOrder.ASC
            else -> SortOrder.DESC
        }

        val searchFilter = search
            ?.takeIf { it.isNotBlank() }
            ?.let {
                (KabupatenTable.kode like "%$it%") or
                        (KabupatenTable.nama like "%$it%")
            }

        val parentFilter = provinsiId?.let {
            KabupatenTable.provinsiId eq it
        }

        val filter = listOfNotNull(
            searchFilter,
            parentFilter
        ).reduceOrNull { acc, condition ->
            acc and condition
        }

        val baseQuery = KabupatenTable
            .innerJoin(ProvinsiTable)
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
                KabupatenListItem(
                    id = it[KabupatenTable.id].value,
                    provinsiId = it[ProvinsiTable.id].value,
                    provinsiNama = it[ProvinsiTable.nama],
                    kode = it[KabupatenTable.kode],
                    nama = it[KabupatenTable.nama],
                    isActive = it[KabupatenTable.isActive]
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

    override suspend fun findAllActiveByProvinsi(
        provinsiId: Short
    ): List<KabupatenDomain> = databaseQuery {
        KabupatenEntity
            .find {
                (KabupatenTable.provinsiId eq provinsiId) and
                        (KabupatenTable.isActive eq true)
            }
            .orderBy(KabupatenTable.nama to SortOrder.ASC)
            .map { it.toKabupatenDomain() }
    }
}