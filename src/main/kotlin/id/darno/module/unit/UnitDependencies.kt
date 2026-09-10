package id.darno.module.unit

import id.darno.core.database.DatabaseQuery
import id.darno.module.unit.repository.CompanyProfileRepository
import id.darno.module.unit.repository.CompanyProfileRepositoryImpl
import id.darno.module.unit.repository.UnitRepository
import id.darno.module.unit.repository.UnitRepositoryImpl
import id.darno.module.unit.service.UnitService
import id.darno.module.unit.service.UnitServiceImpl
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.plugins.di.resolve

fun Application.configureUnitDependencies(){
    dependencies {
        provide<UnitRepository> {
            UnitRepositoryImpl(
                resolve<DatabaseQuery>()
            )
        }
        provide<UnitService> {
            UnitServiceImpl(resolve<UnitRepository>())
        }
        provide<CompanyProfileRepository> {
            CompanyProfileRepositoryImpl(resolve<DatabaseQuery>())
        }
    }
}