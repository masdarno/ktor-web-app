package id.darno.module.user.model

data class UserUnitReportRow(
    val nama: String,
    val username: String,
    val email: String,
    val verified: String?,
    val role: String,
    val unit: String
)