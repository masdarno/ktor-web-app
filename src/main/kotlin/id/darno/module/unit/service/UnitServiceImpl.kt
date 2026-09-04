package id.darno.module.unit.service

import id.darno.module.unit.domain.UnitDomain
import id.darno.module.unit.repository.UnitRepository
import io.ktor.server.plugins.*

class UnitServiceImpl(private val unitRepository: UnitRepository): UnitService {
    override suspend fun getAll(): List<UnitDomain> =
        unitRepository.findAll()

    override suspend fun getById(id: Short): UnitDomain =
        unitRepository.findById(id)
            ?: throw NotFoundException("Unit tidak ditemukan")
}