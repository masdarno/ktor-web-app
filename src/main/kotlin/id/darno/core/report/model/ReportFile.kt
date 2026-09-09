package id.darno.core.report.model

data class ReportFile(
    val content: ByteArray,
    val fileName: String,
    val contentType: String
)