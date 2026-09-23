package id.darno.module.user.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.email
import id.darno.core.validation.rules.notEmpty
import id.darno.module.user.dto.UpdateUserProfileRequest

object UserProfileValidator :
    FormValidator<UpdateUserProfileRequest> {

    override fun validate(
        value: UpdateUserProfileRequest
    ): List<FieldError> {

        val validator = FieldValidator()

        validator.notEmpty(
            value = value.nama,
            field = "nama",
            message = "Nama harus diisi"
        )

        validator.notEmpty(
            value = value.alias,
            field = "alias",
            message = "Alias harus diisi"
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