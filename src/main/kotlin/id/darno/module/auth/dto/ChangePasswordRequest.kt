package id.darno.module.auth.dto

import id.darno.core.validation.valiktor.constraint.isEqualToPassword
import id.darno.core.validation.valiktor.constraint.isNotEqualToPassword
import org.valiktor.functions.hasSize
import org.valiktor.functions.isEqualTo
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isNotEqualTo
import org.valiktor.validate

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val passwordConfirmation: String
) {
    init {
        validate(this) {
            validate(ChangePasswordRequest::newPassword).isNotEmpty().hasSize(min = 6)
            validate(ChangePasswordRequest::passwordConfirmation).isNotEmpty().isEqualToPassword(newPassword)
            validate(ChangePasswordRequest::currentPassword).isNotBlank()
            validate(ChangePasswordRequest::newPassword).isNotEqualToPassword(currentPassword)
        }
    }
}

