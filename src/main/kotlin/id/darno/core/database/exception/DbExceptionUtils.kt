package id.darno.core.database.exception

import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import java.sql.SQLException

internal data class SqlExceptionInfo(
    val sqlState: String?,
    val errorCode: Int?,
    val message: String
)

internal object DbExceptionUtils {

    fun inspect(exception: ExposedSQLException): SqlExceptionInfo {
        val sqlException = findSqlException(exception)

        return SqlExceptionInfo(
            sqlState = sqlException?.sqlState?.uppercase(),
            errorCode = sqlException?.errorCode,
            message = (
                    sqlException?.message
                        ?: exception.message
                        ?: ""
                    ).lowercase()
        )
    }

    private fun findSqlException(
        throwable: Throwable?
    ): SQLException? {
        var current = throwable

        while (current != null) {
            if (current is SQLException) {
                return current
            }

            current = current.cause
        }

        return null
    }

    fun extractConstraint(message: String): String? {
        val patterns = listOf(

            // PostgreSQL:
            // constraint "users_username_key"
            Regex(
                """constraint\s+"([^"]+)"""",
                RegexOption.IGNORE_CASE
            ),

            // MariaDB / MySQL:
            // for key 'users_username_unique'
            Regex(
                """for key\s+'([^']+)"""",
                RegexOption.IGNORE_CASE
            ),

            // MariaDB / MySQL:
            // CONSTRAINT `fk_user_unit`
            Regex(
                """constraint\s+`([^`]+)`""",
                RegexOption.IGNORE_CASE
            ),

            // Generic:
            // constraint 'foo'
            Regex(
                """constraint\s+'([^']+)'""",
                RegexOption.IGNORE_CASE
            )
        )

        return patterns
            .asSequence()
            .mapNotNull { match ->
                match.find(message)
                    ?.groupValues
                    ?.getOrNull(1)
            }
            .firstOrNull()
    }
}