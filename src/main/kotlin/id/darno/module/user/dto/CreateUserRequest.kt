package id.darno.module.user.dto

import org.valiktor.functions.hasSize
import org.valiktor.functions.isEmail
import org.valiktor.functions.isGreaterThan
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class CreateUserRequest(
    val nama: String,
    val alias: String,
    val username: String,
    val email: String,
    val roleId: Short
) {
    init {
        validate(this) {
            validate(CreateUserRequest::nama).isNotEmpty()
            validate(CreateUserRequest::alias).isNotEmpty()
            validate(CreateUserRequest::username).isNotEmpty().hasSize(min = 3, max = 10)
            validate(CreateUserRequest::email).isNotEmpty().isEmail()
            validate(CreateUserRequest::roleId).isGreaterThan(0)
        }
    }
}