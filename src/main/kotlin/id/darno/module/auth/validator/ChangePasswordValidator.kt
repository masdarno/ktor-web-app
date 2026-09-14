package id.darno.module.auth.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.equalsTo
import id.darno.core.validation.rules.minLength
import id.darno.core.validation.rules.notBlank
import id.darno.core.validation.rules.notEmpty
import id.darno.core.validation.rules.notEqualsTo
import id.darno.module.auth.dto.ChangePasswordRequest

object ChangePasswordValidator :
    FormValidator<ChangePasswordRequest> {

    override fun validate(
        value: ChangePasswordRequest
    ): List<FieldError> {
        val validator = FieldValidator()

        validator.notEmpty(
            value = value.newPassword,
            field = "newPassword",
            message = "Password baru harus diisi"
        )

        validator.minLength(
            value = value.newPassword,
            field = "newPassword",
            min = 6,
            message = "Password baru minimal 6 karakter"
        )

        validator.notEmpty(
            value = value.passwordConfirmation,
            field = "passwordConfirmation",
            message = "Konfirmasi password harus diisi"
        )

        validator.equalsTo(
            value = value.passwordConfirmation,
            field = "passwordConfirmation",
            expected = value.newPassword,
            message = "Konfirmasi password tidak sama"
        )

        validator.notBlank(
            value = value.currentPassword,
            field = "currentPassword",
            message = "Password saat ini harus diisi"
        )

        validator.notEqualsTo(
            value = value.newPassword,
            field = "newPassword",
            expected = value.currentPassword,
            message = "Password baru harus berbeda dari password saat ini"
        )

        return validator.errors
    }
}