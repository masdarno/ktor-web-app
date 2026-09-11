package id.darno.core.database.config

import io.ktor.server.application.*
import io.ktor.server.config.*

fun Application.mainDatabaseConfig(): DatabaseConfig =
    environment.config.mainDatabaseConfig()

fun ApplicationConfig.mainDatabaseConfig(): DatabaseConfig {
    val cfg = try {
        config("db")
    } catch (e: ApplicationConfigurationException) {
        throw IllegalStateException(
            "Missing 'db' configuration section in application.yaml", e
        )
    }

    val type = JdbcType.fromString(cfg.requiredString("type"))
    val host = cfg.requiredString("host")
    val port = cfg.optionalInt("port")
    val name = cfg.requiredString("name")
    val user = cfg.requiredString("user")
    val password = cfg.optionalString("password") ?: ""

    return DatabaseConfig(
        type = type,
        host = host,
        port = port,
        name = name,
        user = user,
        password = password
    )
}