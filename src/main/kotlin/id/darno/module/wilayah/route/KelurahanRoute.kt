package id.darno.module.wilayah.route

import id.darno.module.wilayah.controller.KelurahanController
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.configureKelurahanRoute(
    controller: KelurahanController
) {

    get("/wilayah/kelurahan") {
        controller.index(call)
    }

    get("/wilayah/kelurahan/table") {
        controller.table(call)
    }

    // reload dropdown kabupaten (filter tabel) saat provinsi berubah
    get("/wilayah/kelurahan/filter-kabupaten") {
        controller.filterKabupaten(call)
    }

    // reload dropdown kecamatan (filter tabel) saat kabupaten berubah
    get("/wilayah/kelurahan/filter-kecamatan") {
        controller.filterKecamatan(call)
    }

    get("/wilayah/kelurahan/form") {
        controller.form(call)
    }

    post("/wilayah/kelurahan") {
        controller.create(call)
    }

    put("/wilayah/kelurahan/{id}") {

        val id =
            call.parameters["id"]
                ?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

        controller.update(call, id)
    }

    delete("/wilayah/kelurahan/{id}") {

        val id =
            call.parameters["id"]
                ?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

        controller.delete(call, id)
    }
}