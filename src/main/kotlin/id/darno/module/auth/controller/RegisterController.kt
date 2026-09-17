package id.darno.module.auth.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.http.mapper.toFormData
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.validation.toErrorMap
import id.darno.module.auth.mapper.toRegisterRequest
import id.darno.module.auth.validator.RegisterValidator
import id.darno.module.user.service.UserAuthService
import id.darno.module.auth.mapper.toCreateUserParams
import id.darno.module.auth.service.EmailVerificationService
import id.darno.module.user.exception.UserException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

class RegisterController(
    private val userAuthService: UserAuthService,
    private val emailVerificationService: EmailVerificationService
) {

    private val logger =
        LoggerFactory.getLogger(RegisterController::class.java)

    companion object {
        private const val TEMPLATE_PAGE =
            "pages/auth/register.html"

        private const val TEMPLATE_FORM =
            "pages/auth/fragments/register-form.html"

        private const val PAGE_TITLE =
            "Register User"
    }

    suspend fun index(call: ApplicationCall) {
        val csrfToken = call.ensureCsrfToken()

        call.respond(
            PebbleContent(
                TEMPLATE_PAGE,
                mapOf(
                    "title" to PAGE_TITLE,
                    "csrfToken" to csrfToken
                )
            )
        )
    }

    suspend fun register(call: ApplicationCall) {
        val parameters = call.receiveParameters()

        val request = parameters.toRegisterRequest()

        val validationErrors =
            RegisterValidator.validate(request)

        if (validationErrors.isNotEmpty()) {
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData = parameters.toFormData()
            )
        }

        try {
            val user =
                userAuthService.register(
                    request.toCreateUserParams()
                )

            emailVerificationService.sendVerification(
                userId = user.id,
                email = user.email
            )

            call.hxTriggerWithToast(
                "Alhamdulillah, ${user.nama}",
                ToastType.SUCCESS
            )

            call.respond(
                PebbleContent(
                    TEMPLATE_FORM,
                    mapOf(
                        "errors" to emptyMap<String, String>(),
                        "formData" to emptyMap<String, String>()
                    )
                )
            )

        }
        catch (ex: UserException) {
            logger.error("Failed to register user: {}", request.nama, ex)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    ex.field to ex.message
                ),
                formData = parameters.toFormData()
            )
        }
        catch (ex: ApplicationException) {
            logger.error("Failed to register user: {}", request.nama, ex)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    "nama" to (ex.message ?: "Ada kesalahan")
                ),
                formData = parameters.toFormData()
            )
        }
    }
}