package id.darno.module.auth.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.utility.respondUniversalRedirect
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.validation.valiktor.helper.errors
import id.darno.module.auth.dto.ResetPasswordRequest
import id.darno.module.auth.service.PasswordResetService
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory
import org.valiktor.ConstraintViolationException

class ResetPasswordController(
    private val passwordResetService: PasswordResetService
) {

    private val logger = LoggerFactory.getLogger(ResetPasswordController::class.java)

    suspend fun index(call: ApplicationCall) {
        val token = call.request.queryParameters["token"]
            ?: return call.respondUniversalRedirect("/forgot-password")

        // OPTIONAL tapi direkomendasikan:
        val tokenValid = passwordResetService.isTokenValid(token)
        if (!tokenValid) {
            return call.respondUniversalRedirect("/forgot-password")
        }

        call.respond(
            PebbleContent(
                "pages/auth/reset-password.html",
                mapOf(
                    "title" to "Reset Password",
                    "formData" to mapOf("token" to token),
                    "csrfToken" to call.ensureCsrfToken()
                )
            )
        )
    }

    suspend fun handleResetPassword(call: ApplicationCall) {
        val token = call.request.queryParameters["token"]
            ?: return call.respondUniversalRedirect("/forgot-password")

        val params = call.receiveParameters()
        val password = params["password"]?.trim().orEmpty()
        val passwordConfirmation = params["password_confirmation"]?.trim().orEmpty()

        try {
            val request = ResetPasswordRequest(
                token = token,
                password = password,
                passwordConfirmation = passwordConfirmation
            )

            passwordResetService.resetPassword(
                token = request.token,
                newPassword = request.password
            )

            call.respondUniversalRedirect(
                "/login?reset=success"
            )

        } catch (ex: ConstraintViolationException) {
            logger.warn("Reset Password validation failed: {}", ex.constraintViolations)

            throw HtmxFormException(
                templatePath = "pages/auth/fragments/reset-password-form.html",
                errors = ex.errors(),
                formData = mapOf("token" to token)
            )

        } catch (ex: ApplicationException) {
            logger.error("Reset Password FAILED", ex)

            throw HtmxFormException(
                templatePath = "pages/auth/fragments/reset-password-form.html",
                errors = mapOf("password" to (ex.message ?: "Reset password gagal")),
                formData = mapOf("token" to token)
            )
        }
    }

}