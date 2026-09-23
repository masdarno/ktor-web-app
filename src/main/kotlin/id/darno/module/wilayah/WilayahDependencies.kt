package id.darno.module.wilayah

import id.darno.core.database.query.DatabaseQuery
import id.darno.module.wilayah.controller.KabupatenController
import id.darno.module.wilayah.controller.KecamatanController
import id.darno.module.wilayah.controller.KelurahanController
import id.darno.module.wilayah.controller.ProvinsiController
import id.darno.module.wilayah.repository.KabupatenRepository
import id.darno.module.wilayah.repository.KabupatenRepositoryImpl
import id.darno.module.wilayah.repository.KecamatanRepository
import id.darno.module.wilayah.repository.KecamatanRepositoryImpl
import id.darno.module.wilayah.repository.KelurahanRepository
import id.darno.module.wilayah.repository.KelurahanRepositoryImpl
import id.darno.module.wilayah.repository.ProvinsiRepository
import id.darno.module.wilayah.repository.ProvinsiRepositoryImpl
import id.darno.module.wilayah.service.KabupatenService
import id.darno.module.wilayah.service.KabupatenServiceImpl
import id.darno.module.wilayah.service.KecamatanService
import id.darno.module.wilayah.service.KecamatanServiceImpl
import id.darno.module.wilayah.service.KelurahanService
import id.darno.module.wilayah.service.KelurahanServiceImpl
import id.darno.module.wilayah.service.ProvinsiService
import id.darno.module.wilayah.service.ProvinsiServiceImpl
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import io.ktor.server.plugins.di.resolve

fun Application.configureWilayahDependencies() {

    dependencies {

        // =========================
        // Repository
        // =========================

        provide<ProvinsiRepository> {
            ProvinsiRepositoryImpl(
                databaseQuery = resolve<DatabaseQuery>()
            )
        }

        provide<KabupatenRepository> {
            KabupatenRepositoryImpl(
                databaseQuery = resolve<DatabaseQuery>()
            )
        }

        provide<KecamatanRepository> {
            KecamatanRepositoryImpl(
                databaseQuery = resolve<DatabaseQuery>()
            )
        }

        provide<KelurahanRepository> {
            KelurahanRepositoryImpl(
                databaseQuery = resolve<DatabaseQuery>()
            )
        }

        // =========================
        // Service
        // =========================

        provide<ProvinsiService> {
            ProvinsiServiceImpl(
                provinsiRepository = resolve<ProvinsiRepository>()
            )
        }

        provide<KabupatenService> {
            KabupatenServiceImpl(
                kabupatenRepository = resolve<KabupatenRepository>(),
                provinsiRepository = resolve<ProvinsiRepository>()
            )
        }

        provide<KecamatanService> {
            KecamatanServiceImpl(
                kecamatanRepository = resolve<KecamatanRepository>(),
                kabupatenRepository = resolve<KabupatenRepository>()
            )
        }

        provide<KelurahanService> {
            KelurahanServiceImpl(
                kelurahanRepository = resolve<KelurahanRepository>(),
                kecamatanRepository = resolve<KecamatanRepository>()
            )
        }

        // =========================
        // Controller
        // =========================

        provide<ProvinsiController> {
            ProvinsiController(
                provinsiService = resolve<ProvinsiService>()
            )
        }

        provide<KabupatenController> {
            KabupatenController(
                kabupatenService = resolve(),
                provinsiService = resolve()
            )
        }

        provide<KecamatanController> {
            KecamatanController(
                kecamatanService = resolve(),
                kabupatenService = resolve(),
                provinsiService = resolve()
            )
        }

        provide<KelurahanController> {
            KelurahanController(
                kelurahanService = resolve(),
                kecamatanService = resolve(),
                kabupatenService = resolve(),
                provinsiService = resolve()
            )
        }

    }
}