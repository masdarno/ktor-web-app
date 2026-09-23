package id.darno.core.database.config

import io.ktor.server.config.*

internal fun ApplicationConfig.optionalString(path: String): String? =
    propertyOrNull(path)
        ?.getString()
        ?.trim()
        ?.takeIf { it.isNotEmpty() }   // string kosong (env var unset) dianggap tidak ada

internal fun ApplicationConfig.requiredString(path: String): String =
    optionalString(path)
        ?: throw IllegalArgumentException("Required configuration '$path' is missing")

internal fun ApplicationConfig.optionalInt(path: String): Int? {
    val raw = optionalString(path) ?: return null
    return raw.toIntOrNull()
        ?: throw IllegalArgumentException(
            "Invalid integer value for '$path': '$raw'"
        )
}