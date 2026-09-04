package id.darno.module.auth.dto

import org.valiktor.functions.isNotEmpty
import org.valiktor.validate

/**
 * Data Transfer Object (DTO) yang digunakan untuk menerima kredensial
 * (username dan password plain text) dari client saat proses login. Tidak perlu serialize karena dari form
 */
data class LoginRequest(
    val username: String,
    val password: String, // password plain text yang dikirim client
    val rememberMe: Boolean = false
) {
    init {
        validate(this) {
            validate(LoginRequest::username).isNotEmpty()
            validate(LoginRequest::password).isNotEmpty()
        }
    }
}