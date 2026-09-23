package id.darno.core.security.crypto

interface Hasher {

    fun hash(value: String): String

    fun verify(value: String, hash: String): Boolean
}
