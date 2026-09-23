package id.darno.module.user.model

data class CreateUserParams(
    val nama: String,
    val alias: String,
    val username: String,
    val password: String,
    val email: String,
    val photo: String,
    val genderId: Short,
    val roleId: Short
)