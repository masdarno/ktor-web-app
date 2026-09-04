package id.darno.module.role.model

data class RoleCreateParams(
    val name: String,
    val isActive: Boolean = true
)
