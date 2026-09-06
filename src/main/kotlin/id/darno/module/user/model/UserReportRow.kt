package id.darno.module.user.model

data class UserReportRow(
    val nama: String,
    val username: String,
    val email: String,
    val verified: String?,
    val role: String
)