package id.darno.module.auth.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.equalsTo
import id.darno.core.validation.rules.minLength
import id.darno.core.validation.rules.notEmpty
import id.darno.module.auth.dto.ResetPasswordRequest

object ResetPasswordValidator : FormValidator<ResetPasswordRequest> {

    override fun validate(
        value: ResetPasswordRequest
    ): List<FieldError> {
        val validator = FieldValidator()

        validator.notEmpty(
            value = value.token,
            field = "token",
            message = "Token harus diisi"
        )

        validator.notEmpty(
            value = value.password,
            field = "password",
            message = "Password harus diisi"
        )

        validator.minLength(
            value = value.password,
            field = "password",
            min = 6,
            message = "Password minimal 6 karakter"
        )

        validator.notEmpty(
            value = value.passwordConfirmation,
            field = "passwordConfirmation",
            message = "Konfirmasi password harus diisi"
        )

        validator.equalsTo(
            value = value.passwordConfirmation,
            field = "passwordConfirmation",
            expected = value.password,
            message = "Konfirmasi password tidak sama"
        )

        return validator.errors
    }
}