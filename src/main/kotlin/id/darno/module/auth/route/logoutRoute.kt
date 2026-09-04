package id.darno.module.auth.route

import id.darno.core.htmx.utility.respondUniversalRedirect
import id.darno.core.route.plugin.logger
import id.darno.core.session.model.CsrfSession
import id.darno.core.session.model.RememberMeCookie
import id.darno.core.session.model.TempUserSession
import id.darno.core.session.model.UserSession
import id.darno.module.auth.service.RememberMeService
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.logoutRoute(rememberMeService: RememberMeService) {
    // Cek punya salah satu : UserSession or TempUserSession
    authenticate("auth-temp-user-session", "auth-user-session") {
        post("/logout") {
            val rememberMeCookie = call.sessions.get<RememberMeCookie>()

            // Clear semua kemungkinan session
            call.sessions.clear<UserSession>()
            call.sessions.clear<TempUserSession>()
            call.sessions.clear<CsrfSession>()

            rememberMeCookie?.let {
                rememberMeService.revoke(it.selector)
                call.sessions.clear<RememberMeCookie>()
            }

            logger.info("Logout successful for session.")
            call.respondUniversalRedirect("/login")
        }
    }
}