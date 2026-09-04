package id.darno.module.auth

import id.darno.core.config.appConfig
import id.darno.core.mail.MailService
import id.darno.core.security.crypto.Hasher
import id.darno.module.auth.controller.*
import id.darno.module.auth.repository.*
import id.darno.module.auth.service.*
import id.darno.module.unit.service.UnitService
import id.darno.module.user.service.UserAuthService
import id.darno.module.user.service.UserEmailVerificationService
import id.darno.module.user.service.UserLookupService
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureAuthDependencies(){
    val config = appConfig()
    dependencies {
        provide<RememberMeRepository> { RememberMeRepositoryImpl() }
        provide<EmailVerificationRepository> { EmailVerificationRepositoryImpl() }
        provide<PasswordResetRepository>{ PasswordResetRepositoryImpl() }

        provide<EmailVerificationService>{
            EmailVerificationServiceImpl(
                resolve<EmailVerificationRepository>(),
                resolve<UserEmailVerificationService>(),
                resolve<MailService>(),
                config,
                resolve<Hasher>("sha256")
            )
        }
        provide<PasswordResetService>{
            PasswordResetServiceImpl(
                resolve<UserLookupService>(),
                resolve<UserAuthService>(),
                resolve<PasswordResetRepository>(),
                resolve<RememberMeRepository>(),
                resolve<MailService>(),
                config,
                resolve<Hasher>("sha256"),
            )
        }
        provide<RememberMeService> {
            RememberMeServiceImpl(
                resolve<RememberMeRepository>(),
                resolve<UserLookupService>(),
                resolve<UnitService>(),
                resolve<Hasher>("sha256")
            )
        }

        provide<RegisterController> {
            RegisterController(
                resolve<UserAuthService>(),
                resolve<EmailVerificationService>()
            )
        }
        provide<LoginController> {
            LoginController(
                resolve<UserAuthService>(),
                resolve<RememberMeService>()
            )
        }
        provide<SelectUnitController> {
            SelectUnitController(
                resolve<UserLookupService>(),
                resolve<UnitService>(),
                resolve<RememberMeService>()
            )
        }
        provide<EmailVerificationController>{
            EmailVerificationController(
                resolve<EmailVerificationService>(),
                resolve<UserLookupService>()
            )
        }
        provide<ForgotPasswordController>{
            ForgotPasswordController(resolve<PasswordResetService>())
        }
        provide<ResetPasswordController>{
            ResetPasswordController(resolve<PasswordResetService>())
        }
        provide<ChangePasswordController>{
            ChangePasswordController(
                resolve<UserAuthService>(),
                resolve<RememberMeService>()
            )
        }
    }
}