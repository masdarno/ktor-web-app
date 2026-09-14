package id.darno.module.auth.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.utility.respondUniversalRedirect
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.session.model.TempUserSession
import id.darno.core.session.model.UserSession
import id.darno.core.validation.toErrorMap
import id.darno.module.auth.mapper.toChangePasswordRequest
import id.darno.module.auth.service.RememberMeService
import id.darno.module.auth.validator.ChangePasswordValidator
import id.darno.module.user.service.UserAuthService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions

class ChangePasswordController(
    private val userAuthService: UserAuthService,
    private val rememberMeService: RememberMeService
) {

    suspend fun index(call: ApplicationCall) {
        val fromReset =
            call.request.queryParameters["from"] == "reset"

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

    suspend fun handleChangePassword(
        call: ApplicationCall
    ) {
        val session =
            call.sessions.get<UserSession>()
                ?: return call.respondUniversalRedirect(
                    "/login"
                )

        val parameters =
            call.receiveParameters()

        val request =
            parameters.toChangePasswordRequest()

        val validationErrors =
            ChangePasswordValidator.validate(request)

        if (validationErrors.isNotEmpty()) {
            /*
             * Jangan redisplay password.
             */
            throw HtmxFormException(
                templatePath =
                    "pages/auth/fragments/change-password-form.html",
                errors = validationErrors.toErrorMap(),
                formData = emptyMap()
            )
        }

        try {
            userAuthService.changePassword(
                userId = session.userId,
                currentPassword = request.currentPassword,
                newPassword = request.newPassword
            )

            rememberMeService.revokeByUserId(
                session.userId
            )

            call.sessions.clear<UserSession>()
            call.sessions.clear<TempUserSession>()

            call.respondUniversalRedirect(
                "/login?password=changed"
            )

        } catch (ex: ApplicationException) {
            throw HtmxFormException(
                templatePath =
                    "pages/auth/fragments/change-password-form.html",
                errors = mapOf(
                    "currentPassword" to (
                            ex.message
                                ?: "Password saat ini salah"
                            )
                ),
                formData = emptyMap()
            )
        }
    }
}