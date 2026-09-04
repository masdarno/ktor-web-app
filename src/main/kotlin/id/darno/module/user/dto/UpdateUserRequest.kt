package id.darno.module.user.dto

import org.valiktor.functions.isEmail
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class UpdateUserRequest(
    val nama: String,
    val alias: String,
    val username: String,
    val email: String,
    val roleId: Short
) {
    init {
        validate(this) {
            validate(UpdateUserRequest::nama).isNotEmpty()
            validate(UpdateUserRequest::alias).isNotEmpty()
            validate(UpdateUserRequest::username).isNotEmpty()
            validate(UpdateUserRequest::email).isNotEmpty().isEmail()
            validate(UpdateUserRequest::roleId).isGreaterThan(0)
        }
    }
}