package id.darno.module.auth.route

import id.darno.module.auth.controller.EmailVerificationController
import io.ktor.server.routing.*

fun Route.configureEmailVerificationRoute(emailVerificationController: EmailVerificationController){

    get("/verify-email-required") {
        emailVerificationController.index(call)
    }

    post("/resend-verification") {
        emailVerificationController.resendVerification(call)
    }
}