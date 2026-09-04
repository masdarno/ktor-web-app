package id.darno.core.htmx.utility

import id.darno.core.htmx.model.ToastEvent
import id.darno.core.htmx.model.ToastType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

fun ApplicationCall.hxTriggerWithToast(
    message: String,
    type: ToastType = ToastType.INFO,
    name: String = "showToast"
) {
    val toastEvent = ToastEvent(message, type.value)

    val triggerMap = buildMap<String, JsonElement> {
        put(name, Json.encodeToJsonElement(toastEvent))
    }

    response.headers.append(
        "HX-Trigger",
        Json.encodeToString(triggerMap)
    )
}

// Redirect Htmx
suspend fun ApplicationCall.hxRedirectTo(url: String) {
    response.header("HX-Redirect", url)
    respond(HttpStatusCode.NoContent)
}

/**
 * Fungsi Universal untuk melakukan redirect, memilih metode (HTMX atau Standar HTTP)
 * berdasarkan keberadaan header 'HX-Request'.
 */
suspend fun ApplicationCall.respondUniversalRedirect(targetPath: String) {
    // Pengecekan terhadap header HX-Request
    val isHtmx = request.headers["HX-Request"] == "true"

    if (isHtmx) {
        // Jika dari HTMX, gunakan header kustom HTMX (204 No Content)
        hxRedirectTo(targetPath)
    } else {
        // Jika dari browser biasa atau client non-HTMX, gunakan redirect 302 standar
        respondRedirect(targetPath)
    }
}