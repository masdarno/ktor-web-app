package id.darno.module.wilayah.route

import id.darno.module.wilayah.controller.KabupatenController
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put

fun Route.configureKabupatenRoute(
    controller: KabupatenController
) {

    get("/wilayah/kabupaten") {
        controller.index(call)
    }

    get("/wilayah/kabupaten/table") {
        controller.table(call)
    }

    get("/wilayah/kabupaten/form") {
        controller.form(call)
    }

    post("/wilayah/kabupaten") {
        controller.create(call)
    }

    put("/wilayah/kabupaten/{id}") {

        val id =
            call.parameters["id"]
                ?.toShortOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

        controller.update(call, id)
    }

    delete("/wilayah/kabupaten/{id}") {

        val id =
            call.parameters["id"]
                ?.toShortOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)

        controller.delete(call, id)
    }

    get("/wilayah/kabupaten/options") {
        controller.options(call)
    }
}