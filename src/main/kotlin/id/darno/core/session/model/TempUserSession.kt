package id.darno.core.session.model

import kotlinx.serialization.Serializable

// Temporary UserSession
@Serializable
data class TempUserSession(
    val userId: Short,
    val name: String,
    val photoUrl: String,
    val roleId: Short,
    val role: String,
    val isVerified: Boolean = false,
    val unitId: Short? = null,
    val unit: String?,
    val rememberMe: Boolean
)