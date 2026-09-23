package id.darno.module.auth.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.validation.toErrorMap
import id.darno.module.auth.mapper.toForgotPasswordRequest
import id.darno.module.auth.service.PasswordResetService
import id.darno.module.auth.validator.ForgotPasswordValidator
import io.ktor.server.application.ApplicationCall
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

class ForgotPasswordController(
    private val passwordResetService: PasswordResetService
) {

    private val logger =
        LoggerFactory.getLogger(
            ForgotPasswordController::class.java
        )

    companion object {
        private const val TEMPLATE_PAGE =
            "pages/auth/forgot-password.html"

        private const val TEMPLATE_FORM =
            "pages/auth/fragments/forgot-password-form.html"
    }

    suspend fun index(call: ApplicationCall) {
        val csrfToken = call.ensureCsrfToken()

        call.respond(
            PebbleContent(
                TEMPLATE_PAGE,
                mapOf(
                    "title" to "Forgot Password",
                    "csrfToken" to csrfToken
                )
            )
        )
    }

    suspend fun handleForgotPassword(
        call: ApplicationCall
    ) {
        val parameters = call.receiveParameters()

        val request =
            parameters.toForgotPasswordRequest()

        val validationErrors =
            ForgotPasswordValidator.validate(request)

        if (validationErrors.isNotEmpty()) {
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData = mapOf(
                    "email" to request.email
                )
            )
        }

        try {
            passwordResetService.requestReset(
                request.email
            )

            call.respond(
                PebbleContent(
                    TEMPLATE_FORM,
                    mapOf(
                        "status" to
                                "If the email is registered, " +
                                "a password reset link has been sent.\n" +
                                "Please check your inbox."
                    )
                )
            )

        } catch (ex: ApplicationException) {
            logger.error(
                "Failed to handle forgot password, email: {}",
                request.email,
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    "email" to (
                            ex.message
                                ?: "Ada kesalahan"
                            )
                ),
                formData = mapOf(
                    "email" to request.email
                )
            )
        }
    }
}