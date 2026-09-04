package id.darno.module.auth.model

/**
 * Model internal yang HANYA digunakan untuk tujuan otentikasi (login/verifikasi).
 * Model ini memuat HASH password.
 */
data class UserCredentials(
    val id: Short, // Sesuaikan dengan tipe ID Anda
    val username: String,
    val passwordHash: String, // Nama field yang jelas menunjukkan ini adalah hash
    val isActive: Boolean
)