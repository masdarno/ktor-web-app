package id.darno.core.session.model

import kotlinx.serialization.Serializable

@Serializable
data class RememberMeCookie(
    val selector: String,
    val validator: String
)