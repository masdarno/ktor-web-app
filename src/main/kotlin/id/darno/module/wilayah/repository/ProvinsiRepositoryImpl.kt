package id.darno.module.wilayah.repository

import id.darno.core.database.query.DatabaseQuery
import id.darno.core.pageddata.model.PagedResult
import id.darno.module.user.database.dao.UserEntity
import id.darno.module.wilayah.database.dao.ProvinsiEntity
import id.darno.module.wilayah.database.table.ProvinsiTable
import id.darno.module.wilayah.domain.ProvinsiDomain
import id.darno.module.wilayah.mapper.toProvinsiDomain
import id.darno.module.wilayah.model.CreateProvinsiParams
import id.darno.module.wilayah.model.ProvinsiListItem
import id.darno.module.wilayah.model.UpdateProvinsiParams
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class ProvinsiRepositoryImpl(
    private val databaseQuery: DatabaseQuery
) : ProvinsiRepository {

    override suspend fun create(
        params: CreateProvinsiParams
    ): ProvinsiDomain = databaseQuery {

        ProvinsiEntity.new {
            kode = params.kode
            nama = params.nama
            isActive = params.isActive
            createdBy = UserEntity[params.createdBy]
        }.toProvinsiDomain()
    }

    override suspend fun findById(
        id: Short
    ): ProvinsiDomain? = databaseQuery {
        ProvinsiEntity.findById(id)
            ?.toProvinsiDomain()
    }

    override suspend fun findByKode(
        kode: String
    ): ProvinsiDomain? = databaseQuery {
        ProvinsiEntity
            .find { ProvinsiTable.kode eq kode }
            .singleOrNull()
            ?.toProvinsiDomain()
    }

    override suspend fun existsByKode(
        kode: String
    ): Boolean = databaseQuery {
        ProvinsiEntity
            .find { ProvinsiTable.kode eq kode }
            .any()
    }

    override suspend fun existsByNama(
        nama: String
    ): Boolean = databaseQuery {
        ProvinsiEntity
            .find { ProvinsiTable.nama eq nama }
            .any()
    }

    override suspend fun update(
        id: Short,
        params: UpdateProvinsiParams
    ): ProvinsiDomain = databaseQuery {

        val entity = ProvinsiEntity[id]

        entity.apply {
            kode = params.kode ?: kode
            nama = params.nama ?: nama
            isActive = params.isActive ?: isActive
            updatedBy = UserEntity[params.updatedBy]
        }.toProvinsiDomain()
    }

    override suspend fun delete(
        id: Short
    ): Boolean = databaseQuery {

        ProvinsiTable.deleteWhere {
            ProvinsiTable.id eq id
        }

        true
    }

    override suspend fun findAll(
        search: String?,
        page: Int,
        pageSize: Int,
        sortBy: String,
        sortDir: String
    ): PagedResult<ProvinsiListItem> = databaseQuery {

        val sortColumn = when (sortBy) {
            "kode" -> ProvinsiTable.kode
            "nama" -> ProvinsiTable.nama
            "active" -> ProvinsiTable.isActive
            else -> ProvinsiTable.id
        }

        val order = when (sortDir.lowercase()) {
            "asc" -> SortOrder.ASC
            else -> SortOrder.DESC
        }

        val filter = search
            ?.takeIf { it.isNotBlank() }
            ?.let {
                (ProvinsiTable.kode like "%$it%") or
                        (ProvinsiTable.nama like "%$it%")
            }

        val baseQuery = ProvinsiTable
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
                ProvinsiListItem(
                    id = it[ProvinsiTable.id].value,
                    kode = it[ProvinsiTable.kode],
                    nama = it[ProvinsiTable.nama],
                    isActive = it[ProvinsiTable.isActive]
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

    override suspend fun findAllActive(): List<ProvinsiDomain> = databaseQuery {
        ProvinsiEntity
            .find { ProvinsiTable.isActive eq true }
            .orderBy(ProvinsiTable.nama to SortOrder.ASC)
            .map { it.toProvinsiDomain() }
    }
}