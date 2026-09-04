package id.darno.module.auth.dto

import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class ForgotPasswordRequest(val email: String){
    init {
        validate(this){
            validate(ForgotPasswordRequest::email).isNotEmpty().isEmail()
        }
    }
}
