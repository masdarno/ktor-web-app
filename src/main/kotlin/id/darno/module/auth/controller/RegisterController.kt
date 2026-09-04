package id.darno.module.auth.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.http.mapper.toFormData
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.validation.valiktor.helper.errors
import id.darno.module.auth.dto.RegisterRequest
import id.darno.module.auth.mapper.toCreateUserParams
import id.darno.module.auth.service.EmailVerificationService
import id.darno.module.user.service.UserAuthService
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory
import org.valiktor.ConstraintViolationException

class RegisterController(
    private val userAuthService: UserAuthService,
    private val emailVerificationService: EmailVerificationService) {

    private val logger = LoggerFactory.getLogger(RegisterController::class.java)

    companion object {
        private const val TEMPLATE_PAGE = "pages/auth/register.html"
        private const val TEMPLATE_FORM = "pages/auth/fragments/register-form.html"
        private const val PAGE_TITLE = "Register User"
    }

    suspend fun index(call: ApplicationCall){
        val csrfToken = call.ensureCsrfToken()
        call.respond(PebbleContent(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "csrfToken" to csrfToken
            )
        ))
    }

    suspend fun register(call: ApplicationCall){
        val parameters = call.receiveParameters()
        try {
            val request = RegisterRequest(
                name = parameters["name"].orEmpty(),
                username = parameters["username"].orEmpty(),
                password = parameters["password"].orEmpty(),
                passwordConfirmation = parameters["passwordConfirmation"].orEmpty(),
                email = parameters["email"].orEmpty(),
            )

            val user = userAuthService.register(request.toCreateUserParams())

            // Kirim email verifikasi
            emailVerificationService.sendVerification(
                userId = user.id,
                email = user.email
            )

            call.hxTriggerWithToast(
                "Alhamdulillah, ${user.name}",
                ToastType.SUCCESS
            )
            call.respond(
                PebbleContent(
                    TEMPLATE_FORM,
                    mapOf(
                        "errors" to emptyList<String>(),
                        "formData" to emptyMap<String, String>()
                    )
                )
            )

        } catch (ex: ConstraintViolationException) {
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = ex.errors(),
                formData = parameters.toFormData()
            )
        } catch (ex: ApplicationException) {
            // ERROR SERVICE/REPOSITORY
            logger.error("Failed to register user: {}", parameters["name"], ex)

            val key = when {
                ex.message?.contains("username", ignoreCase = true) == true -> "username"
                ex.message?.contains("email", ignoreCase = true) == true -> "email"
                else -> "name"
            }

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(key to (ex.message ?: "Ada kesalahan")),
                formData = parameters.toFormData()
            )
        }
    }
}
