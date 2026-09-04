package id.darno.module.user.config

import io.ktor.server.application.*

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

fun Application.userModuleConfig(): UserModuleConfig {
    val config = environment.config.config("modules.user")

    val profileDir = config
        .config("upload")
        .property("profile-dir")
        .getString()

    val maxSizeMb = config
        .config("photo")
        .property("max-size-mb")
        .getString()
        .toLong()

    val uploadUrl = config
        .config("photo-url")
        .property("upload-dir")
        .getString()

    val defaultUrl = config
        .config("photo-url")
        .property("default-dir")
        .getString()

    return UserModuleConfig(
        upload = UserUploadConfig(
            profileDir = profileDir,
            maxPhotoSizeBytes = maxSizeMb * 1024 * 1024
        ),
        photoUrl = PhotoUrlConfig(
            upload = uploadUrl,
            default = defaultUrl
        )
    )
}