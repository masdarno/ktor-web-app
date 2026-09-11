package id.darno.core.database.config

import io.ktor.server.application.*
import io.ktor.server.config.*

fun Application.reportingDatabaseConfig(): DatabaseConfig =
    environment.config.reportingDatabaseConfig(mainDatabaseConfig())

fun ApplicationConfig.reportingDatabaseConfig(mainConfig: DatabaseConfig): DatabaseConfig {
    val cfg = try {
        config("db.reporting")
    } catch (e: ApplicationConfigurationException) {
        throw IllegalStateException(
            "Missing 'db.reporting' configuration section in application.yaml", e
        )
    }

    return DatabaseConfig(
        type = mainConfig.type,
        host = cfg.optionalString("host") ?: mainConfig.host,
        port = cfg.optionalInt("port") ?: mainConfig.port,
        name = cfg.optionalString("name") ?: mainConfig.name,
        user = cfg.requiredString("user"),
        password = cfg.optionalString("password") ?: ""
    )
}