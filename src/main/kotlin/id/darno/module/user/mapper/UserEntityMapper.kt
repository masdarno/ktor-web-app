package id.darno.module.user.mapper

import id.darno.core.model.DefaultValues
import id.darno.module.auth.model.UserCredentials
import id.darno.module.user.config.PhotoUrlConfig
import id.darno.module.user.database.dao.UserEntity
import id.darno.module.user.domain.UserDomain

// Fungsi konversi dari UserEntity (DAO) ke User (Model/DTO).
fun UserEntity.toUserDomain(config: PhotoUrlConfig): UserDomain {
    // Logika accessor photoUrl:
    val photoUrl = if (this.photo in setOf(DefaultValues.DEFAULT_MALE_PHOTO, DefaultValues.DEFAULT_FEMALE_PHOTO)) {
        "${config.default}/${this.photo}"
    } else {
        // Jika kolom 'photo' berisi nama file, gabungkan Route Upload + Nama File
        "${config.upload}/${this.photo}"
    }
    return UserDomain(
        id = this.id.value,
        nama = this.nama,
        alias = this.alias,
        username = this.username,
        genderId = this.genderId,
        email = this.email,
        isVerified = this.emailVerifiedAt != null,
        photo = this.photo,
        photoUrl = photoUrl,
        roleId = this.role.id.value,
        role = this.role.nama,
        isActive = this.isActive
    )
}

// Fungsi konversi khusus untuk kebutuhan login
fun UserEntity.toUserCredentials(): UserCredentials {
    return UserCredentials(
        id = id.value,
        username = username,
        passwordHash = password, // Mengambil hash dari kolom password
        isActive = isActive
    )
}