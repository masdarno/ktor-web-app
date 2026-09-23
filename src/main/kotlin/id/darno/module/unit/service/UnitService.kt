package id.darno.module.unit.service

import id.darno.module.unit.domain.UnitDomain

interface UnitService {
    suspend fun getAll(): List<UnitDomain>

    suspend fun getById(id: Short): UnitDomain
}