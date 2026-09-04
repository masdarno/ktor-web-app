package id.darno.core.storage.config

import io.ktor.server.application.*
import java.io.File

enum class StorageType {
    LOCAL
}

data class StorageConfig(
    val type: StorageType,
    val local: LocalStorageConfig
)

data class LocalStorageConfig(
    val basePath: File
)

fun Application.storageConfig(): StorageConfig {
    val config = environment.config.config("storage")

    val typeString = config.property("type").getString().uppercase()
    val type = StorageType.valueOf(typeString)

    // Ambil config local
    val basePath = config.property("base-path").getString()

    return StorageConfig(
        type = type,
        local = LocalStorageConfig(basePath = File(basePath))
    )
}