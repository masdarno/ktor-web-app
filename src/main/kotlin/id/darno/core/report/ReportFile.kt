package id.darno.core.report

data class ReportFile(
    val content: ByteArray,
    val fileName: String,
    val contentType: String
)