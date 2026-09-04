package id.darno.module.auth.dto

import id.darno.core.validation.valiktor.constraint.isEqualToPassword
import org.valiktor.functions.hasSize
import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class RegisterRequest(
    val nama: String,
    val username: String,
    val password: String,
    val passwordConfirmation: String,
    val email: String
) {
    init {
        validate(this) {
            validate(RegisterRequest::nama).isNotEmpty()
            validate(RegisterRequest::username).isNotEmpty().hasSize(min = 3, max = 10)
            validate(RegisterRequest::password).isNotEmpty().hasSize(min = 6)
            validate(RegisterRequest::passwordConfirmation).isNotEmpty().isEqualToPassword(password)
            validate(RegisterRequest::email).isNotEmpty().isEmail()
        }
    }
}