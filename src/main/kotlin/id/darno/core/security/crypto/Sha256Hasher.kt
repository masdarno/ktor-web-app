package id.darno.core.security.crypto

import org.slf4j.LoggerFactory
import java.security.MessageDigest

class Sha256Hasher(
    private val pepper: String
) : Hasher {
    private val logger = LoggerFactory.getLogger(Sha256Hasher::class.java)
    override fun hash(value: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((value + pepper).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun verify(value: String, hash: String): Boolean {
        logger.info("SHA256_HASHER verify value : {}", value)
        return constantTimeEquals(hash(value), hash)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}