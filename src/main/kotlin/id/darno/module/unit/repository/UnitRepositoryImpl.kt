package id.darno.module.unit.repository

import id.darno.core.database.dbQuery
import id.darno.module.unit.database.dao.UnitEntity
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.unit.mapper.toUnitDomain

class UnitRepositoryImpl: UnitRepository {
    override suspend fun findAll(): List<UnitDomain> = dbQuery {
        UnitEntity.all().map { it.toUnitDomain() }
    }

    override suspend fun findById(id: Short): UnitDomain? = dbQuery {
        UnitEntity.findById(id)?.toUnitDomain()
    }
}