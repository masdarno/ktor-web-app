package id.darno.module.user.dto

data class CreateUserRequest(
    val nama: String,
    val alias: String,
    val username: String,
    val email: String,
    val roleId: Short
)