package id.darno.module.user.dto

import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class UpdateUserProfileRequest(
    val name: String,
    val alias: String,
    val email: String
) {
    init {
        validate(this) {
            validate(UpdateUserProfileRequest::name).isNotEmpty()
            validate(UpdateUserProfileRequest::alias).isNotEmpty()
            validate(UpdateUserProfileRequest::email).isNotEmpty().isEmail()
        }
    }
}

fun UpdateUserProfileRequest.toFormData() = mapOf(
    "name" to name,
    "alias" to alias,
    "email" to email
)