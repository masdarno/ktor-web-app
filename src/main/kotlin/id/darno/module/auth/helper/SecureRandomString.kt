package id.darno.module.auth.helper

import java.security.SecureRandom
import java.util.*

object SecureRandomString {

    private val random = SecureRandom()

    fun generate(bytes: Int): String {
        val buffer = ByteArray(bytes)
        random.nextBytes(buffer)

        // URL-safe, cookie-safe
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(buffer)
    }
}
