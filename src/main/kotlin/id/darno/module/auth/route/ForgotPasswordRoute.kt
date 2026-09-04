package id.darno.module.auth.route

import id.darno.module.auth.controller.ForgotPasswordController
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

// GuestRoute
fun Route.configureForgotPasswordRoute(controller: ForgotPasswordController) {

    get("/forgot-password") {
        controller.index(call)
    }

    post("/forgot-password") {
        controller.handleForgotPassword(call)
    }
}