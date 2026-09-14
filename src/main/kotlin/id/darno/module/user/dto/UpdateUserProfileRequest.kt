package id.darno.module.user.dto

data class UpdateUserProfileRequest(
    val nama: String,
    val alias: String,
    val email: String
)

fun UpdateUserProfileRequest.toFormData(): Map<String, String> =
    mapOf(
        "nama" to nama,
        "alias" to alias,
        "email" to email
    )