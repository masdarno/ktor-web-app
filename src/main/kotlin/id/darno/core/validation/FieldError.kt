package id.darno.core.validation

import kotlinx.serialization.Serializable

@Serializable
data class FieldError(
    val field: String,
    val message: String
)