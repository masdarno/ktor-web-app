package id.darno.module.user.model

data class UserListItem(
    val id: Short,
    val name: String,
    val username: String,
    val email: String,
    val roleName: String
)