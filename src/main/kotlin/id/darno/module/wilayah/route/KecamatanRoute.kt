package id.darno.module.wilayah.route

import id.darno.module.wilayah.controller.KecamatanController
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.configureKecamatanRoute(
    controller: KecamatanController
) {

    get("/wilayah/kecamatan") {
        controller.index(call)
    }

    get("/wilayah/kecamatan/table") {
        controller.table(call)
    }

    // reload dropdown kabupaten (filter tabel) saat provinsi berubah
    get("/wilayah/kecamatan/filter-kabupaten") {
        controller.filterKabupaten(call)
    }

    get("/wilayah/kecamatan/form") {
        controller.form(call)
    }

    post("/wilayah/kecamatan") {
        controller.create(call)
    }

    put("/wilayah/kecamatan/{id}") {

        val id =
            call.parameters["id"]
                ?.toShortOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

        controller.update(call, id)
    }

    delete("/wilayah/kecamatan/{id}") {

        val id =
            call.parameters["id"]
                ?.toShortOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

        controller.delete(call, id)
    }

    // Untuk Kelurahan
    get("/wilayah/kecamatan/options") {
        controller.options(call)
    }
}