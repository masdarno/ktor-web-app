package id.darno.module.unit.repository

import id.darno.module.unit.domain.UnitDomain

interface UnitRepository {
    suspend fun findAll(): List<UnitDomain>

    suspend fun findById(id: Short): UnitDomain?
}