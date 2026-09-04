package id.darno.module.unit

import id.darno.module.unit.repository.UnitRepository
import id.darno.module.unit.repository.UnitRepositoryImpl
import id.darno.module.unit.service.UnitService
import id.darno.module.unit.service.UnitServiceImpl
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureUnitDependencies(){
    dependencies {
        provide<UnitRepository> { UnitRepositoryImpl() }
        provide<UnitService> {
            UnitServiceImpl(resolve<UnitRepository>())
        }
    }
}