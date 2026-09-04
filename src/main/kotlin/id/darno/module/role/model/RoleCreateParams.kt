package id.darno.module.role.model

data class RoleCreateParams(
    val nama: String,
    val isActive: Boolean = true
)
