package id.darno.module.auth.helper

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respondLoginError(
    formData: Map<String, String>,
    message: String
) {
    this.respond(
        HttpStatusCode.OK,
        PebbleContent(
            "pages/auth/fragments/login-form.html",
            mapOf(
                "errors" to mapOf("username" to message),
                "formData" to formData
            )
        )
    )
}