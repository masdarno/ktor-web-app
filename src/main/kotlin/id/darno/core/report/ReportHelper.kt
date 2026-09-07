package id.darno.core.report

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.response.respondBytes

/**
 * disposition = attachment (download), inline (preview)
 */
suspend fun ApplicationCall.respondReport(
    report: ReportFile,
    disposition: String = "inline"
) {
    response.header(
        HttpHeaders.ContentDisposition,
        "$disposition; filename=\"${report.fileName}\""
    )

    respondBytes(
        bytes = report.content,
        contentType = ContentType.parse(report.contentType)
    )
}