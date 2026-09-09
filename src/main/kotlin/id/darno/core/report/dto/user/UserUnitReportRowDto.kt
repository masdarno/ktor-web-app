package id.darno.core.report.dto.user

data class UserUnitReportRowDto(
    val nama: String,
    val username: String,
    val email: String,
    val verified: String?,
    val role: String,
    val unit: String
)