package id.darno.core.htmx.model

import kotlinx.serialization.Serializable

@Serializable
data class ToastEvent(
    val message: String,
    val type: String
)