package id.darno.module.auth.route

import id.darno.module.auth.controller.ChangePasswordController
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.configureChangePasswordRoute(changePasswordController: ChangePasswordController){

    get("/change-password"){
        changePasswordController.index(call)
    }
    post("/change-password"){
        changePasswordController.handleChangePassword(call)
    }

}