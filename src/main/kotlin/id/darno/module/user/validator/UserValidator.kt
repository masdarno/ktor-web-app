package id.darno.module.user.validator

import id.darno.core.validation.FieldError
import id.darno.core.validation.FieldValidator
import id.darno.core.validation.FormValidator
import id.darno.core.validation.rules.email
import id.darno.core.validation.rules.lengthBetween
import id.darno.core.validation.rules.notEmpty
import id.darno.module.user.dto.CreateUserRequest
import id.darno.module.user.dto.UpdateUserRequest

object CreateUserValidator : FormValidator<CreateUserRequest> {

    override fun validate(
        value: CreateUserRequest
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
            value = value.email,
            field = "email",
            message = "Email harus diisi"
        )

        validator.email(
            value = value.email,
            field = "email"
        )

        validator.check(
            value = value.roleId,
            field = "roleId",
            message = "Role harus dipilih"
        ) {
            it > 0
        }

        return validator.errors
    }
}


object UpdateUserValidator : FormValidator<UpdateUserRequest> {

    override fun validate(
        value: UpdateUserRequest
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
            value = value.username,
            field = "username",
            message = "Username harus diisi"
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

        validator.check(
            value = value.roleId,
            field = "roleId",
            message = "Role harus dipilih"
        ) {
            it > 0
        }

        return validator.errors
    }
}