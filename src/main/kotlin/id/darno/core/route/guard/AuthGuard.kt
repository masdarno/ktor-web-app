package id.darno.core.route.guard

import id.darno.core.route.plugin.Guest
import id.darno.core.route.plugin.ResetPassword
import id.darno.core.route.plugin.TempLogin
import io.ktor.server.auth.*
import io.ktor.server.routing.*

/**
 * Helper internal untuk membuat scope routing yang terisolasi.
 * Menambahkan 'suspend' pada fungsi evaluate untuk kompatibilitas Ktor terbaru.
 */
private fun Route.isolatedScope(build: Route.() -> Unit): Route {
    return createChild(object : RouteSelector() {
        // Tambahkan keyword 'suspend' di sini
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    }).apply(build)
}

/**
 * Guest Routes: /login, /register, /forgot-password
 */
fun Route.guestGuard(build: Route.() -> Unit) {
    isolatedScope {
        install(Guest)
        build()
    }
}

/**
 * Password Reset Route: /reset-password
 */
fun Route.resetPasswordGuard(build: Route.() -> Unit) {
    isolatedScope {
        install(ResetPassword)
        build()
    }
}

/**
 * Select Unit Route: /select-unit
 */
fun Route.selectUnitGuard(build: Route.() -> Unit) {
    authenticate("auth-select-unit") {
        install(TempLogin)
        build()
    }
}

/**
 * Email Verification Route
 */
fun Route.emailVerificationGuard(build: Route.() -> Unit) {
    authenticate("auth-verify-email") {
        install(TempLogin)
        build()
    }
}

/**
 * Authenticated Routes: /, /dashboard, /profile, dll
 */
fun Route.authenticatedGuard(build: Route.() -> Unit) {
    authenticate("auth-user-session") {
        build()
    }
}