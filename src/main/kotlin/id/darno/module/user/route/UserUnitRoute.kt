package id.darno.module.user.route

import id.darno.module.user.controller.UserController
import id.darno.module.user.controller.UserUnitController
import io.ktor.http.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("USER_UNIT_ROUTE")

fun Route.configureUserUnitRoute(userUnitController: UserUnitController){

    get("/user-unit"){
        userUnitController.index(call)
    }

    // load fragmen htmx table
    get("/user-unit/table"){
        userUnitController.table(call)
    }
}