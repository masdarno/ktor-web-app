package id.darno.module.auth.route

import id.darno.module.auth.controller.RegisterController
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("REGISTER_ROUTE")

fun Route.configureRegisterRoute(registerController: RegisterController) {

    get("/register") {
        registerController.index(call)
    }
    post("/register"){
        registerController.register(call)
    }
}