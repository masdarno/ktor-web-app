package id.darno.module.user.exception

import id.darno.core.exceptions.ApplicationException
import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.service.ServiceException
import id.darno.module.user.database.table.UserTable

sealed class UserException(
    override val message: String,
    open val field: String = "general",
    cause: Throwable? = null
) : ServiceException(message, cause) {

    class UsernameAlreadyExists(
        val username: String,
        cause: Throwable? = null
    ) : UserException("Username $username sudah ada", field = "username", cause)

    class EmailAlreadyExists(
        val email: String,
        cause: Throwable? = null
    ) : UserException("Email $email sudah ada", field = "email", cause)

    class RoleNotFound(
        val roleId: Short,
        cause: Throwable? = null
    ) : UserException("Role tidak ditemukan", field = "roleId", cause)

    class GenderNotFound(
        val genderId: Short,
        cause: Throwable? = null
    ) : UserException("Gender tidak ditemukan", field = "genderId", cause)

    class UserInUse(
        cause: Throwable? = null
    ) : UserException("User tidak dapat dihapus karena masih memiliki data terkait di sistem", cause = cause)

}

/**
 * Jaring pengaman untuk race condition: pre-check existsByUsername/existsByEmail
 * lolos, tapi INSERT/UPDATE tetap gagal karena unique constraint di DB.
 * Memetakan DuplicateKeyException (hasil DbExceptionMapper) ke typed exception
 * berdasarkan nama constraint, bukan menebak dari pesan.
 */
fun DuplicateKeyException.toUserException(
    username: String? = null,
    email: String? = null
): ApplicationException {
    // Normalisasi: beberapa versi MariaDB melaporkan 'users.username',
    // yang lain cukup 'username'. Ambil segmen terakhir saja.
    val key = constraint?.substringAfterLast(".")

    return when (key) {
        UserTable.USERNAME_UNIQUE_CONSTRAINT ->
            UserException.UsernameAlreadyExists(username ?: "", cause = this)

        UserTable.EMAIL_UNIQUE_CONSTRAINT ->
            UserException.EmailAlreadyExists(email ?: "", cause = this)

        else -> this
    }
}

fun ForeignKeyException.toUserException(
    roleId: Short? = null,
    genderId: Short? = null
): ApplicationException {
    val key = constraint?.substringAfterLast(".")

    return when (key) {
        UserTable.ROLE_FK_CONSTRAINT ->
            UserException.RoleNotFound(roleId ?: 0, cause = this)

        UserTable.GENDER_FK_CONSTRAINT ->
            UserException.GenderNotFound(genderId ?: 0, cause = this)

        else -> this
    }
}