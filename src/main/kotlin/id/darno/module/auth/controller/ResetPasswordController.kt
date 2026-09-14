package id.darno.module.auth.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.utility.respondUniversalRedirect
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.validation.toErrorMap
import id.darno.module.auth.mapper.toResetPasswordRequest
import id.darno.module.auth.service.PasswordResetService
import id.darno.module.auth.validator.ResetPasswordValidator
import io.ktor.server.application.ApplicationCall
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

class ResetPasswordController(
    private val passwordResetService: PasswordResetService
) {

    private val logger =
        LoggerFactory.getLogger(
            ResetPasswordController::class.java
        )

    suspend fun index(call: ApplicationCall) {
        val token =
            call.request.queryParameters["token"]
                ?: return call.respondUniversalRedirect(
                    "/forgot-password"
                )

        val tokenValid =
            passwordResetService.isTokenValid(token)

        if (!tokenValid) {
            return call.respondUniversalRedirect(
                "/forgot-password"
            )
        }

        call.respond(
            PebbleContent(
                "pages/auth/reset-password.html",
                mapOf(
                    "title" to "Reset Password",
                    "formData" to mapOf(
                        "token" to token
                    ),
                    "csrfToken" to call.ensureCsrfToken()
                )
            )
        )
    }

    suspend fun handleResetPassword(
        call: ApplicationCall
    ) {
        val token =
            call.request.queryParameters["token"]
                ?: return call.respondUniversalRedirect(
                    "/forgot-password"
                )

        val parameters =
            call.receiveParameters()

        val request =
            parameters.toResetPasswordRequest(token)

        val validationErrors =
            ResetPasswordValidator.validate(request)

        if (validationErrors.isNotEmpty()) {
            throw HtmxFormException(
                templatePath =
                    "pages/auth/fragments/reset-password-form.html",
                errors = validationErrors.toErrorMap(),
                formData = mapOf(
                    "token" to token
                )
            )
        }

        try {
            passwordResetService.resetPassword(
                token = request.token,
                newPassword = request.password
            )

            call.respondUniversalRedirect(
                "/login?reset=success"
            )

        } catch (ex: ApplicationException) {
            logger.error(
                "Reset Password FAILED",
                ex
            )

            throw HtmxFormException(
                templatePath =
                    "pages/auth/fragments/reset-password-form.html",
                errors = mapOf(
                    "password" to (
                            ex.message
                                ?: "Reset password gagal"
                            )
                ),
                formData = mapOf(
                    "token" to token
                )
            )
        }
    }
}