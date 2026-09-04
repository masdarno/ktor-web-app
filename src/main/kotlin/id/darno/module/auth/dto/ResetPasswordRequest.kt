package id.darno.module.auth.dto

import id.darno.core.validation.valiktor.constraint.isEqualToPassword
import org.valiktor.functions.hasSize
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class ResetPasswordRequest(
    val token: String,
    val password: String,
    val passwordConfirmation: String,
){
    init {
        validate(this){
            validate(ResetPasswordRequest::token).isNotEmpty()
            validate(ResetPasswordRequest::password).isNotEmpty().hasSize(6)
            validate(ResetPasswordRequest::passwordConfirmation).isNotEmpty().isEqualToPassword(password)
        }
    }
}
