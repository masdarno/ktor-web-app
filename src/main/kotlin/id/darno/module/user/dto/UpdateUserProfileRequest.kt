package id.darno.module.user.dto

import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

data class UpdateUserProfileRequest(
    val nama: String,
    val alias: String,
    val email: String
) {
    init {
        validate(this) {
            validate(UpdateUserProfileRequest::nama).isNotEmpty()
            validate(UpdateUserProfileRequest::alias).isNotEmpty()
            validate(UpdateUserProfileRequest::email).isNotEmpty().isEmail()
        }
    }
}

fun UpdateUserProfileRequest.toFormData() = mapOf(
    "nama" to nama,
    "alias" to alias,
    "email" to email
)