package id.darno.core.security.crypto

import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory

class BCryptHasher(
    private val logRounds: Int = 10
) : Hasher {
    private val logger = LoggerFactory.getLogger(BCryptHasher::class.java)

    override fun hash(value: String): String {
        val salt = BCrypt.gensalt(logRounds)
        return BCrypt.hashpw(value, salt)
    }

    override fun verify(value: String, hash: String): Boolean {
        logger.info("BCRYPT_HASHER verify value : {}", value)
        return BCrypt.checkpw(value, hash)
    }
}