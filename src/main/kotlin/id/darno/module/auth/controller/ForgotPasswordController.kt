package id.darno.module.auth.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.validation.valiktor.helper.errors
import id.darno.module.auth.dto.ForgotPasswordRequest
import id.darno.module.auth.service.PasswordResetService
import io.ktor.server.application.ApplicationCall
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory
import org.valiktor.ConstraintViolationException

class ForgotPasswordController(private val passwordResetService: PasswordResetService) {

    private val logger = LoggerFactory.getLogger(ForgotPasswordController::class.java)

    suspend fun index(call: ApplicationCall) {
        val csrfToken = call.ensureCsrfToken()
        call.respond(PebbleContent(
            "pages/auth/forgot-password.html",
            mapOf(
                "title" to "Forgot Password",
                "csrfToken" to csrfToken
            )
        ))
    }

    suspend fun handleForgotPassword(call: ApplicationCall) {
        val email = call.receiveParameters()["email"] ?: ""
        try {
            val request = ForgotPasswordRequest(email)
            passwordResetService.requestReset(request.email)
            call.respond(
                PebbleContent(
                    "pages/auth/fragments/forgot-password-form.html",
                    mapOf("status" to "If the email is registered, a password reset link has been sent.\n" +
                            "Please check your inbox.")
                )
            )
        } catch (ex: ConstraintViolationException) {
            // ERROR VALIDASI VALIKTOR
            logger.warn("Validation failed for user creation: {}", ex.constraintViolations)

            throw HtmxFormException(
                templatePath = "pages/auth/fragments/forgot-password-form.html",
                errors = ex.errors(),
                formData = mapOf("email" to email),
            )
        } catch (ex: ApplicationException) {
            // ERROR SERVICE/REPOSITORY
            logger.error("Failed to handle forgat password, email: {}", email, ex)

            throw HtmxFormException(
                templatePath = "pages/auth/fragments/forgot-password-form.html",
                errors = mapOf("email" to (ex.message ?: "Ada kesalahan")),
                formData = mapOf("email" to email)
            )
        }
    }
}