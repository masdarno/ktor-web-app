package id.darno.module.auth.route

import id.darno.module.auth.controller.ResetPasswordController
import io.ktor.server.routing.*

fun Route.configureResetPasswordRoute(controller: ResetPasswordController) {

    //PasswordResetRoute
    get("/reset-password") {
        controller.index(call)
    }

    post("/reset-password") {
        controller.handleResetPassword(call)
    }
}