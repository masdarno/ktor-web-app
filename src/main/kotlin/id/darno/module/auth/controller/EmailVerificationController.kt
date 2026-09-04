package id.darno.module.auth.controller

import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.session.model.TempUserSession
import id.darno.module.auth.service.EmailVerificationService
import id.darno.module.user.service.UserLookupService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.slf4j.LoggerFactory

class EmailVerificationController(
    private val emailVerificationService: EmailVerificationService,
    private val userLookupService: UserLookupService
) {

    private val logger = LoggerFactory.getLogger(EmailVerificationController::class.java)

    suspend fun index(call: ApplicationCall){

        val session = call.sessions.get<TempUserSession>()
            ?: run {
                call.respondRedirect("/login")
                return
            }

        val user = userLookupService.getById(session.userId)

        if (user.isVerified) {
            call.respondRedirect("/select-unit")
            return
        }

        call.respondPebblePage(
            "pages/auth/verify-email-required.html",
            mapOf(
                "user" to user
            )
        )
    }

    suspend fun resendVerification(call: ApplicationCall){
        val session = call.sessions.get<TempUserSession>()
            ?: error("Unauthorized")

        val user = userLookupService.getById(session.userId)

        if (user.isVerified) {
            call.respondText(
                "Email sudah diverifikasi",
                ContentType.Text.Html
            )
            return
        }

        emailVerificationService.sendVerification(
            userId = user.id,
            email = user.email
        )

        call.respondText(
            "<div class='alert alert-success'>Email verifikasi telah dikirim</div>",
            ContentType.Text.Html
        )
    }
}