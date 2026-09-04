package id.darno.module.auth.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.utility.hxRedirectTo
import id.darno.core.http.mapper.toFormData
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.session.helper.regenerateCsrfToken
import id.darno.core.session.model.TempUserSession
import id.darno.core.validation.valiktor.helper.errors
import id.darno.module.auth.dto.LoginRequest
import id.darno.module.auth.helper.respondLoginError
import id.darno.module.auth.mapper.combineWith
import id.darno.module.auth.mapper.toFormData
import id.darno.module.auth.mapper.toLoginParams
import id.darno.module.auth.model.AuthResult
import id.darno.module.auth.service.RememberMeService
import id.darno.module.user.service.UserAuthService
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.slf4j.LoggerFactory
import org.valiktor.ConstraintViolationException

class LoginController(
    private val userAuthService: UserAuthService,
    private val rememberMeService: RememberMeService
    ) {

    private val logger = LoggerFactory.getLogger(LoginController::class.java)

    companion object {
        private const val TEMPLATE_PAGE = "pages/auth/login.html"
        private const val TEMPLATE_FORM = "pages/auth/fragments/login-form.html"
        private const val PAGE_TITLE = "Login"
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

    suspend fun login(call: ApplicationCall) {
        val parameters = call.receiveParameters()

        try {
            val request = LoginRequest(
                username = parameters["username"].orEmpty(),
                password = parameters["password"].orEmpty(),
                rememberMe = parameters["remember_me"].orEmpty() == "1"
            )

            when (val result = userAuthService.verifyCredentials(request.toLoginParams())) {
                is AuthResult.Success -> {
                    logger.info("login sukses: {}", request.username)
                    val user = result.user
                    val units = result.unit

                    if (units.size == 1 && user.isVerified) {
                        val session = user.combineWith(units.first())
                        call.sessions.set(session)
                        // If Remember me
                        if (request.rememberMe) {
                            val (token, cookie) = rememberMeService.issueToken(user.id, units.first().id)
                            rememberMeService.save(token)
                            call.sessions.set(cookie)
                        }
                        // Regenerate CSRF Token
                        call.regenerateCsrfToken()
                        // redirect
                        call.hxRedirectTo("/dashboard")
                    } else {
                        val unitId = if (units.size == 1) units.first().id else null
                        val unit = if(units.size == 1) units.first().nama else null
                        val session = TempUserSession(
                            userId = user.id,
                            nama = user.nama,
                            photoUrl = user.photoUrl,
                            roleId = user.roleId,
                            role = user.role,
                            isVerified = user.isVerified,
                            unitId = unitId,
                            unit = unit,
                            rememberMe = request.rememberMe
                        )
                        call.sessions.set(session)
                        if(user.isVerified) {
                            call.hxRedirectTo("/select-unit")
                        } else {
                            call.hxRedirectTo("/verify-email-required")
                        }
                    }
                }

                AuthResult.InvalidCredentials -> {
                    call.respondLoginError(
                        request.toFormData(),
                        "Kredensial salah!"
                    )
                }

                AuthResult.UserInactive -> {
                    call.respondLoginError(
                        request.toFormData(),
                        "Akun Anda tidak aktif!"
                    )
                }
            }
        } catch (ex: ConstraintViolationException) {
            logger.error("Validation failed: {}", ex.constraintViolations)
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = ex.errors(),
                formData = parameters.toFormData()
            )
        } catch (ex: ApplicationException) {
            logger.error("Login error", ex)

            call.respondLoginError(
                parameters.toFormData(),
                "Terjadi kesalahan server."
            )
        }
    }
}