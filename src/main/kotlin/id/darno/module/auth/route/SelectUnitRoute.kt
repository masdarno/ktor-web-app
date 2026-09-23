package id.darno.module.auth.route

import id.darno.module.auth.controller.SelectUnitController
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("SELECT_UNIT_ROUTE")

fun Route.configureSelectUnitRoute(selectUnitController: SelectUnitController){

    // -- GET /select-unit
    get("/select-unit") {
        selectUnitController.index(call)
    }

    // -- POST /select-unit
    post("/select-unit") {
        selectUnitController.selectUnit(call)
    }
}