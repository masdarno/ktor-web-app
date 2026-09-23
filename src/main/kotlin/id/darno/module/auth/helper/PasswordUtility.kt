package id.darno.module.auth.helper

import org.mindrot.jbcrypt.BCrypt

/**
 * Kelas utilitas untuk menangani operasi keamanan,
 * terutama hashing dan verifikasi password menggunakan BCrypt.
 */
object PasswordUtility {

    // Faktor beban kerja (workload factor) BCrypt.
    // Nilai default adalah 10. Nilai yang lebih tinggi lebih aman, tetapi lebih lambat.
    private const val BCRYPT_LOG_ROUNDS = 10

    /**
     * Mengubah password plain text menjadi hash yang aman menggunakan BCrypt.
     * * @param plainPassword Password dalam bentuk teks biasa.
     * @return String hash password yang sudah dienkripsi.
     */
    fun hashPassword(plainPassword: String): String {
        // 1. Membuat salt secara acak
        val salt = BCrypt.gensalt(BCRYPT_LOG_ROUNDS)

        // 2. Menggabungkan salt dan password lalu melakukan hashing
        return BCrypt.hashpw(plainPassword, salt)
    }

    /**
     * Memverifikasi apakah password plain text yang dimasukkan
     * cocok dengan hash password yang tersimpan di database.
     * * @param plainPassword Password yang dimasukkan oleh pengguna saat login (plain text).
     * @param hashedPassword Hash password yang diambil dari database.
     * @return Boolean, true jika cocok, false jika tidak.
     */
    fun checkPassword(plainPassword: String, hashedPassword: String): Boolean {
        // BCrypt.checkpw secara otomatis mengekstrak salt dari hash
        // dan melakukan hashing lalu membandingkannya.
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }
}