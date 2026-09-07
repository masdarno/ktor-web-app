package id.darno.core.report

enum class ReportFormat(
    val extension: String,
    val contentType: String
) {

    PDF(
        extension = "pdf",
        contentType = "application/pdf"
    ),

    XLSX(
        extension = "xlsx",
        contentType =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ),

    CSV(
        extension = "csv",
        contentType = "text/csv"
    )
}