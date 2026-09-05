package id.darno.module.user.route

import id.darno.module.user.controller.UserUnitController
import io.ktor.server.plugins.BadRequestException
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

    // load form modal (checkbox user yang belum terdaftar di unit)
    get("/user-unit/form"){
        userUnitController.form(call)
    }

    // submit checkbox -> insert ke user_units
    post("/user-unit"){
        userUnitController.store(call)
    }

    delete("/user-unit/{userId}") {

        val userId =
            call.parameters["userId"]
                ?.toShortOrNull()
                ?: throw BadRequestException("userId tidak valid")

        val unitId =
            call.request.queryParameters["unitId"]
                ?.toShortOrNull()
                ?: throw BadRequestException("unitId tidak valid")

        userUnitController.delete(call, userId, unitId)
    }
}