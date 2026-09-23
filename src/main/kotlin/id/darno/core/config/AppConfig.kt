package id.darno.core.config

import io.ktor.server.application.Application

data class AppConfig(
    val baseUrl: String
)

fun Application.appConfig(): AppConfig{
    val config = environment.config

    val baseUrl = config.property("app.baseUrl").getString()

    return AppConfig(
        baseUrl = baseUrl
    )
}