package id.darno.module.wilayah.route

import id.darno.module.wilayah.controller.ProvinsiController
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.configureProvinsiRoute(
    controller: ProvinsiController
) {

    get("/wilayah/provinsi") {
        controller.index(call)
    }

    get("/wilayah/provinsi/table") {
        controller.table(call)
    }

    get("/wilayah/provinsi/form") {
        controller.form(call)
    }

    post("/wilayah/provinsi") {
        controller.create(call)
    }

    put("/wilayah/provinsi/{id}") {

        val id =
            call.parameters["id"]
                ?.toShortOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

        controller.update(call, id)
    }

    delete("/wilayah/provinsi/{id}") {

        val id =
            call.parameters["id"]
                ?.toShortOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

        controller.delete(call, id)
    }
}