package id.darno.module.auth.route

import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.session.helper.regenerateCsrfToken
import id.darno.core.session.model.TempUserSession
import id.darno.module.auth.mapper.toUserSession
import id.darno.module.auth.service.EmailVerificationService
import id.darno.module.auth.service.RememberMeService
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("VERIFY_EMAIL_ROUTE")

fun Route.configureVerifyEmailPublicRoute(emailVerificationService: EmailVerificationService, rememberMeService: RememberMeService) {

    get("/verify-email") {
        val token = call.request.queryParameters["token"]

        if (token == null) {
            call.respondPebblePage(
                "pages/auth/verify-result.html",
                mapOf("success" to false)
            )
            return@get
        }

        // 1. Eksekusi Verifikasi di Database
        val result = emailVerificationService.verify(token)

        val tempSession = call.sessions.get<TempUserSession>()
        val isAlreadyLoggedIn = tempSession != null

        if (result.isSuccess) {
            tempSession?.let {
                if(it.unitId != null){
                    val userSession =  tempSession.toUserSession()
                    call.sessions.set(userSession)
                    call.sessions.clear<TempUserSession>()

                    // If Remember me
                    if (tempSession.rememberMe) {
                        val (token, cookie) = rememberMeService.issueToken(userSession.userId, userSession.unitId)
                        rememberMeService.save(token)
                        call.sessions.set(cookie)
                    }

                    // Regenerate CSRF Token
                    call.regenerateCsrfToken()
                }
                call.sessions.set(it.copy(isVerified = true))
            }
        }

        call.respondPebblePage(
            "pages/auth/verify-result.html",
            mapOf(
                "success" to result.isSuccess,
                "isLoggedIn" to isAlreadyLoggedIn
            )
        )
    }
}