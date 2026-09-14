package id.darno.module.auth.dto

data class LoginRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean = false
)