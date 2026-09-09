package id.darno.core.report.dto.user

data class UserReportRowDto(
    val nama: String,
    val username: String,
    val email: String,
    val verified: String?,
    val role: String
)