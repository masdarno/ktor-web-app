package id.darno.core.database

import io.ktor.server.application.*
import io.ktor.server.config.*

data class DatabaseConfig(
    val type: JdbcType,
    val host: String,
    val port: Int?,
    val name: String,
    val user: String,
    val password: String
) {
    val jdbcUrl: String
        get() = "${type.prefix}://${host}:${port ?: type.defaultPort}/${name}"

    val driver: String
        get() = type.driver

    // Validasi tambahan setelah objek dibuat
    init {
        require(host.isNotBlank()) { "Database host cannot be blank" }
        require(name.isNotBlank()) { "Database name cannot be blank" }
        require(user.isNotBlank()) { "Database user cannot be blank" }
        // password boleh kosong di beberapa kasus (misalnya trust auth Postgres), jadi tidak wajib
        if (port != null) require(port in 1..65535) { "Port must be between 1 and 65535" }
    }
}

fun Application.databaseConfig(): DatabaseConfig {
    val dbConfig: ApplicationConfig = try {
        environment.config.config("db")
    } catch (e: ApplicationConfigurationException) {  // atau ConfigException di beberapa versi
        throw IllegalStateException("Missing 'db' configuration section in application.conf", e)
    }

    // Extension functions pada ApplicationConfig
    fun ApplicationConfig.requiredString(path: String): String =
        propertyOrNull(path)?.getString()?.trim()
            ?: throw IllegalArgumentException("Required configuration 'db.$path' is missing")

    fun ApplicationConfig.optionalInt(path: String): Int? =
        propertyOrNull(path)?.getString()?.trim()?.toIntOrNull()
            ?: run {
                // Jika property ada tapi value bukan integer, throw error
                propertyOrNull(path)?.let {
                    throw IllegalArgumentException("Invalid integer value for 'db.$path'")
                }
                null
            }

    val type = JdbcType.fromString(dbConfig.requiredString("type"))
    val host = dbConfig.requiredString("host")
    val port = dbConfig.optionalInt("port")
    val name = dbConfig.requiredString("name")
    val user = dbConfig.requiredString("user")

    // Password optional (bisa kosong atau tidak ada)
    val password = dbConfig.propertyOrNull("password")?.getString()?.trim() ?: ""

    return DatabaseConfig(
        type = type,
        host = host,
        port = port,
        name = name,
        user = user,
        password = password
    )
}