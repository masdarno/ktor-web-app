package id.darno.module.unit.repository

import id.darno.core.database.query.DatabaseQuery
import id.darno.module.unit.database.dao.UnitEntity
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.unit.mapper.toUnitDomain

class UnitRepositoryImpl(
    private val databaseQuery: DatabaseQuery
) : UnitRepository {
    override suspend fun findAll(): List<UnitDomain> = databaseQuery {
        UnitEntity.all().map { it.toUnitDomain() }
    }

    override suspend fun findById(id: Short): UnitDomain? = databaseQuery {
        UnitEntity.findById(id)?.toUnitDomain()
    }
}