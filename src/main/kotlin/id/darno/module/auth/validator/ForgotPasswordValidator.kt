package id.darno.module.auth.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.email
import id.darno.core.validation.rules.notEmpty
import id.darno.module.auth.dto.ForgotPasswordRequest

object ForgotPasswordValidator : FormValidator<ForgotPasswordRequest> {

    override fun validate(
        value: ForgotPasswordRequest
    ): List<FieldError> {
        val validator = FieldValidator()

        validator.notEmpty(
            value = value.email,
            field = "email",
            message = "Email harus diisi"
        )

        validator.email(
            value = value.email,
            field = "email"
        )

        return validator.errors
    }
}