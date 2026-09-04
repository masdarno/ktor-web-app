package id.darno.core.route.plugin

import id.darno.core.htmx.utility.respondUniversalRedirect
import id.darno.core.session.model.TempUserSession
import id.darno.core.session.model.UserSession
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("ROUTING_MIDDLEWARE")!!

/**
 * Guest Plugin: Hanya bisa diakses jika TIDAK ada UserSession maupun TempUserSession.
 * Jika ada session (USER_SESSION), user diarahkan ke /dashboard.
 * Jika ada session TEMP_USER_SESSION diarahkan ke /select-unit
 */
val Guest = createRouteScopedPlugin(
    name = "GuestAccess",
) {
    onCall { call ->
        // Cek apakah UserSession sudah ada
        val userSession = call.sessions.get<UserSession>()
        if (userSession != null) {
            call.respondUniversalRedirect("/dashboard")
            return@onCall
        }

        // Cek apakah TempUserSession sudah ada
        val tempUserSession = call.sessions.get<TempUserSession>()
        if (tempUserSession != null) {
            if(tempUserSession.isVerified){
                call.respondUniversalRedirect("/select-unit")
                return@onCall
            }
            else {
                call.respondUniversalRedirect("/verify-email-required")
                return@onCall
            }
        }
    }
}

/**
 * Select Unit Plugin: Memastikan TempUserSession ada TAPI UserSession (lengkap) TIDAK ada.
 * Jika UserSession (lengkap) sudah ada, redirect ke /dashboard.
 */
val TempLogin = createRouteScopedPlugin(
    name = "TempLogin",
) {
    onCall { call ->
        // Cek apakah UserSession sudah ada
        val userSession = call.sessions.get<UserSession>()
        if (userSession != null) {
            call.respondUniversalRedirect("/dashboard")
            return@onCall
        }
    }
}

val ResetPassword = createRouteScopedPlugin(
    name = "ResetPassword"
) {
    onCall { call ->
        val userSession = call.sessions.get<UserSession>()
        if (userSession != null) {
            // User sudah login → reset token tidak boleh override session
            call.respondUniversalRedirect("/change-password?from=reset")
            return@onCall
        }

        val tempSession = call.sessions.get<TempUserSession>()
        if (tempSession != null) {
            // Login belum selesai → tidak masuk akal reset
            call.respondUniversalRedirect("/select-unit")
            return@onCall
        }

    }
}
