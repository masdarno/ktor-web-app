package id.darno.module.auth.mapper

import id.darno.module.auth.dto.ChangePasswordRequest
import id.darno.module.auth.dto.ForgotPasswordRequest
import id.darno.module.auth.dto.LoginRequest
import id.darno.module.auth.dto.RegisterRequest
import id.darno.module.auth.dto.ResetPasswordRequest
import io.ktor.http.Parameters

fun Parameters.toRegisterRequest(): RegisterRequest =
    RegisterRequest(
        nama = this["nama"].orEmpty(),
        username = this["username"].orEmpty(),
        password = this["password"].orEmpty(),
        passwordConfirmation = this["passwordConfirmation"].orEmpty(),
        email = this["email"].orEmpty()
    )

fun Parameters.toLoginRequest(): LoginRequest =
    LoginRequest(
        username = this["username"].orEmpty(),
        password = this["password"].orEmpty(),
        rememberMe = this["remember_me"] == "1"
    )

fun Parameters.toForgotPasswordRequest(): ForgotPasswordRequest =
    ForgotPasswordRequest(
        email = this["email"].orEmpty()
    )

fun Parameters.toResetPasswordRequest(
    token: String
): ResetPasswordRequest =
    ResetPasswordRequest(
        token = token,
        password = this["password"]?.trim().orEmpty(),
        passwordConfirmation =
            this["password_confirmation"]?.trim().orEmpty()
    )

fun Parameters.toChangePasswordRequest(): ChangePasswordRequest =
    ChangePasswordRequest(
        currentPassword =
            this["current_password"]?.trim().orEmpty(),
        newPassword =
            this["password"]?.trim().orEmpty(),
        passwordConfirmation =
            this["password_confirmation"]?.trim().orEmpty()
    )