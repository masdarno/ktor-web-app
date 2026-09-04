package id.darno.module.auth.route

import id.darno.module.auth.controller.LoginController
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*

fun Route.configureLoginRoute(loginController: LoginController){
    get("/login") {
        loginController.index(call)
    }
    rateLimit(RateLimitName("login-limiter")) {
        post("/login"){
            loginController.login(call)
        }
    }
}