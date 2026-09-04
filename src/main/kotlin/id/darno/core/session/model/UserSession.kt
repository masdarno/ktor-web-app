package id.darno.core.session.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSession (
    val userId: Short,
    val nama: String,
    val photoUrl: String,
    val roleId: Short,
    val role: String,
    val unitId: Short,
    val unit: String
)