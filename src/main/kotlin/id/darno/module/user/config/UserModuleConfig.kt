package id.darno.module.user.config

import io.ktor.server.application.*
import io.ktor.server.config.*

data class UserUploadConfig(
    val profileDir: String,
    val maxPhotoSizeBytes: Long
)

data class PhotoUrlConfig(
    val upload: String,
    val default: String
)

data class UserModuleConfig(
    val upload: UserUploadConfig,
    val photoUrl: PhotoUrlConfig
)

/**
 * Extension pada ApplicationConfig (bukan Application), supaya bisa dipakai
 * baik dari server (lewat environment.config) maupun dari CLI
 * (lewat ApplicationConfig("application.yaml") langsung).
 */
fun ApplicationConfig.userPhotoUrlConfig(): PhotoUrlConfig {
    val photoUrlConfig = this
        .config("modules.user")
        .config("photo-url")

    val uploadUrl = photoUrlConfig
        .property("upload-dir")
        .getString()

    val defaultUrl = photoUrlConfig
        .property("default-dir")
        .getString()

    return PhotoUrlConfig(
        upload = uploadUrl,
        default = defaultUrl
    )
}

fun ApplicationConfig.userUploadConfig(): UserUploadConfig {
    val userConfig = this.config("modules.user")

    val profileDir = userConfig
        .config("upload")
        .property("profile-dir")
        .getString()

    val maxSizeMb = userConfig
        .config("photo")
        .property("max-size-mb")
        .getString()
        .toLong()

    return UserUploadConfig(
        profileDir = profileDir,
        maxPhotoSizeBytes = maxSizeMb * 1024 * 1024
    )
}

fun ApplicationConfig.userModuleConfig(): UserModuleConfig =
    UserModuleConfig(
        upload = userUploadConfig(),
        photoUrl = userPhotoUrlConfig()
    )

/**
 * Tetap dipertahankan untuk kompatibilitas kode Application yang sudah ada
 * (mis. UserDependencies.kt), tinggal delegasi ke environment.config.
 */
fun Application.userModuleConfig(): UserModuleConfig =
    environment.config.userModuleConfig()