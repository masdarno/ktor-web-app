package id.darno.core.session.helper

import id.darno.core.session.model.CsrfSession
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import java.util.*

/**
 * Ensure CsrfSession exists.
 * Create one if missing and return csrf token.
 */
fun ApplicationCall.ensureCsrfToken(): String {
    val existing = sessions.get<CsrfSession>()
    if (existing != null) {
        return existing.token
    }

    val newToken = UUID.randomUUID().toString()
    sessions.set(CsrfSession(newToken))

    return newToken
}


fun ApplicationCall.regenerateCsrfToken() {
    val newToken = UUID.randomUUID().toString()
    sessions.set(CsrfSession(newToken))
}