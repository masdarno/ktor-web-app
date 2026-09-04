package id.darno.module.auth.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.utility.respondUniversalRedirect
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.session.model.TempUserSession
import id.darno.core.session.model.UserSession
import id.darno.core.validation.valiktor.helper.errors
import id.darno.module.auth.dto.ChangePasswordRequest
import id.darno.module.auth.service.RememberMeService
import id.darno.module.user.service.UserAuthService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.valiktor.ConstraintViolationException

class ChangePasswordController(
    private val userAuthService: UserAuthService,
    private val rememberMeService: RememberMeService
) {

    suspend fun index(call: ApplicationCall) {
        val fromReset = call.request.queryParameters["from"] == "reset"

        call.respond(
            PebbleContent(
                "pages/auth/change-password.html",
                mapOf(
                    "title" to "Change Password",
                    "csrfToken" to call.ensureCsrfToken(),
                    "fromReset" to fromReset
                )
            )
        )
    }

    suspend fun handleChangePassword(call: ApplicationCall) {
        val session = call.sessions.get<UserSession>()
            ?: return call.respondUniversalRedirect("/login")

        val params = call.receiveParameters()
        val currentPassword = params["current_password"]?.trim().orEmpty()
        val newPassword = params["password"]?.trim().orEmpty()
        val confirmPassword = params["password_confirmation"]?.trim().orEmpty()

        try {
            // 2️⃣ Validation DTO
            ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword,
                passwordConfirmation = confirmPassword
            )

            // 3️⃣ Business logic
            userAuthService.changePassword(
                userId = session.userId,
                currentPassword = currentPassword,
                newPassword = newPassword
            )

            // 🔐 Invalidate ALL remember-me
            rememberMeService.revokeByUserId(session.userId)

            // 4️⃣ Logout semua session
            call.sessions.clear<UserSession>()
            call.sessions.clear<TempUserSession>()

            call.respondUniversalRedirect(
                "/login?password=changed"
            )

        } catch (ex: ConstraintViolationException) {
            throw HtmxFormException(
                templatePath = "pages/auth/fragments/change-password-form.html",
                errors = ex.errors(),
                formData = emptyMap()
            )

        } catch (ex: ApplicationException) {
            throw HtmxFormException(
                templatePath = "pages/auth/fragments/change-password-form.html",
                errors = mapOf(
                    "currentPassword" to (ex.message ?: "Password saat ini salah")
                ),
                formData = emptyMap()
            )
        }
    }

}