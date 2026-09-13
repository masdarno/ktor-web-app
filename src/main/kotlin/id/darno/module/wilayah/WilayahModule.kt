package id.darno.module.wilayah

import id.darno.core.route.guard.authenticatedGuard
import id.darno.module.wilayah.controller.KabupatenController
import id.darno.module.wilayah.controller.KecamatanController
import id.darno.module.wilayah.controller.KelurahanController
import id.darno.module.wilayah.controller.ProvinsiController
import id.darno.module.wilayah.route.configureKabupatenRoute
import id.darno.module.wilayah.route.configureKecamatanRoute
import id.darno.module.wilayah.route.configureKelurahanRoute
import id.darno.module.wilayah.route.configureProvinsiRoute
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing

fun Application.configureWilayahModule() {

    configureWilayahDependencies()

    routing {

        val provinsiController: ProvinsiController by dependencies
        val kabupatenController: KabupatenController by dependencies
        val kecamatanController: KecamatanController by dependencies
        val kelurahanController: KelurahanController by dependencies

        authenticatedGuard {

            configureProvinsiRoute(
                provinsiController
            )

            configureKabupatenRoute(
                kabupatenController
            )

            configureKecamatanRoute(
                kecamatanController
            )

            configureKelurahanRoute(
                kelurahanController
            )
        }
    }
}