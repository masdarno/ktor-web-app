package id.darno.module.user.route

import id.darno.module.user.controller.UserProfileController
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("USER_PROFILE_ROUTE")

fun Route.configureUserProfileRoute(userProfileController: UserProfileController){

    get("/user-profile"){
        userProfileController.index(call)
    }

    put("/user-profile/{id}") {
        val userId = call.parameters["id"]?.toShortOrNull()
            ?: return@put call.respond(HttpStatusCode.BadRequest)

        userProfileController.update(call, userId)
    }
}