package id.darno.module.auth.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.notEmpty
import id.darno.module.auth.dto.LoginRequest

object LoginValidator : FormValidator<LoginRequest> {

    override fun validate(
        value: LoginRequest
    ): List<FieldError> {
        val validator = FieldValidator()

        validator.notEmpty(
            value = value.username,
            field = "username",
            message = "Username harus diisi"
        )

        validator.notEmpty(
            value = value.password,
            field = "password",
            message = "Password harus diisi"
        )

        return validator.errors
    }
}