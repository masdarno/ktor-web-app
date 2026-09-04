package id.darno.module.user.model

import kotlinx.datetime.LocalDateTime

data class UpdateUserParams(
    val name: String? = null,
    val alias: String? = null,
    val username: String? = null,
    val password: String? = null,
    val email: String? = null,
    val emailVerifiedAt: LocalDateTime? = null,
    val genderId: Short? = null,
    val photo: String? = null,
    val roleId: Short? = null,
    val isActive: Boolean? = null
)
