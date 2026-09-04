package id.darno.module.user.route

import id.darno.module.user.controller.UserController
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("USER_ROUTE")

fun Route.configureUserRoute(userController: UserController){

    get("/users"){
        userController.index(call)
    }

    // load fragmen htmx table
    get("/users/table"){
        userController.table(call)
    }

    // load form modal (add/edit/view)
    get("/users/form"){
        userController.form(call)
    }

    // create
    post("/users"){
        userController.create(call)
    }

    // update
    put("/users/{id}") {
        val userId = call.parameters["id"]?.toShortOrNull()
            ?: return@put call.respond(HttpStatusCode.BadRequest)

        userController.update(call, userId)
    }

    // delete
    delete("/users/{id}") {
        val userId = call.parameters["id"]?.toShortOrNull()
            ?: return@delete call.respond(HttpStatusCode.BadRequest)

        userController.delete(call, userId)
    }
}