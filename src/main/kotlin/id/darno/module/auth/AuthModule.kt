package id.darno.module.auth

import id.darno.core.route.guard.authenticatedGuard
import id.darno.core.route.guard.emailVerificationGuard
import id.darno.core.route.guard.guestGuard
import id.darno.core.route.guard.resetPasswordGuard
import id.darno.core.route.guard.selectUnitGuard
import id.darno.module.auth.controller.*
import id.darno.module.auth.route.*
import id.darno.module.auth.service.EmailVerificationService
import id.darno.module.auth.service.RememberMeService
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*

fun Application.configureAuthModule() {

    routing {
        val registerController: RegisterController by dependencies
        val loginController: LoginController by dependencies
        val selectUnitController: SelectUnitController by dependencies
        val emailVerificationController: EmailVerificationController by dependencies
        val forgotPasswordController: ForgotPasswordController by dependencies
        val resetPasswordController: ResetPasswordController by dependencies
        val changePasswordController: ChangePasswordController by dependencies

        val emailVerificationService: EmailVerificationService by dependencies
        val rememberMeService: RememberMeService by dependencies

        guestGuard {
            configureRegisterRoute(registerController)
            configureLoginRoute(loginController)
            configureForgotPasswordRoute(forgotPasswordController)
        }
        selectUnitGuard {
            configureSelectUnitRoute(selectUnitController)
        }
        emailVerificationGuard {
            configureEmailVerificationRoute(emailVerificationController)
        }
        resetPasswordGuard {
            configureResetPasswordRoute(resetPasswordController)
        }
        authenticatedGuard {
            configureChangePasswordRoute(changePasswordController)
        }

        configureVerifyEmailPublicRoute(emailVerificationService, rememberMeService)// Public tanpa guard
        logoutRoute(rememberMeService)
    }
}