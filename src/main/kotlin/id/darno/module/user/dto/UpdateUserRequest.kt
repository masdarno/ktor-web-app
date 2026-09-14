package id.darno.module.user.dto

data class UpdateUserRequest(
    val nama: String,
    val alias: String,
    val username: String,
    val email: String,
    val roleId: Short
)