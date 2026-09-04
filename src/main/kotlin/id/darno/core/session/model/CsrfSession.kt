package id.darno.core.session.model

import kotlinx.serialization.Serializable

@Serializable
data class CsrfSession(val token: String)