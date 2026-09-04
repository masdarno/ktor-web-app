package id.darno.module.user.domain

data class UserDomain(
    val id: Short,
    val name: String,
    val alias: String = "",
    val username: String,
    val genderId: Short = 2,
    val email: String,
    val isVerified: Boolean,
    val photo: String,
    val photoUrl: String,
    val roleId: Short,
    val role: String,
    val isActive: Boolean = true
)