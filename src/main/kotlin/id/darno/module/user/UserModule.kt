package id.darno.module.user

import id.darno.core.route.guard.authenticatedGuard
import id.darno.module.user.controller.UserController
import id.darno.module.user.controller.UserProfileController
import id.darno.module.user.controller.UserUnitController
import id.darno.module.user.route.configureUserProfileRoute
import id.darno.module.user.route.configureUserRoute
import id.darno.module.user.route.configureUserUnitRoute
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*

fun Application.configureUserModule() {


    routing {
        val userController: UserController by dependencies
        val userProfileController: UserProfileController by dependencies
        val userUnitController: UserUnitController by dependencies
        authenticatedGuard {
            configureUserProfileRoute(userProfileController)
            configureUserRoute(userController)
            configureUserUnitRoute(userUnitController)
        }
    }
}