package id.darno.module.auth.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.email
import id.darno.core.validation.rules.equalsTo
import id.darno.core.validation.rules.lengthBetween
import id.darno.core.validation.rules.minLength
import id.darno.core.validation.rules.notBlank
import id.darno.core.validation.rules.notEmpty
import id.darno.module.auth.dto.RegisterRequest

object RegisterValidator : FormValidator<RegisterRequest> {

    override fun validate(
        value: RegisterRequest
    ): List<FieldError> {
        val validator = FieldValidator()

        validator.notEmpty(
            value = value.nama,
            field = "nama",
            message = "Nama harus diisi"
        )

        validator.notEmpty(
            value = value.username,
            field = "username",
            message = "Username harus diisi"
        )

        validator.lengthBetween(
            value = value.username,
            field = "username",
            range = 3..10,
            message = "Username harus 3–10 karakter"
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