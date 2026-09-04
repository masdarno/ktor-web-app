package id.darno.core.database

import id.darno.core.exceptions.repository.DataIntegrityException
import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.repository.RepositoryException
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException

object DbExceptionMapper {
    fun map(e: ExposedSQLException): RepositoryException {
        val msg = e.message?.lowercase() ?: ""
        val sqlState = e.sqlState?.uppercase()

        return when {
            isDuplicateKey(msg, sqlState) ->
                DuplicateKeyException("Data sudah ada / duplikat", e)

            isForeignKeyViolation(msg, sqlState) ->
                ForeignKeyException("Referensi data tidak valid", e)

            isNotNullViolation(msg, sqlState) ->
                NotNullViolationException("Field wajib tidak boleh kosong", e)

            isCheckConstraintViolation(msg, sqlState) ->
                CheckConstraintException("Data tidak memenuhi constraint", e)

            isConnectionError(msg, sqlState) ->
                DatabaseConnectionException("Koneksi database bermasalah", e)

            isTimeoutError(msg, sqlState) ->
                DatabaseTimeoutException("Database timeout", e)

            isSyntaxError(msg, sqlState) ->
                DatabaseSyntaxException("Query syntax error", e)

            else ->
                DataIntegrityException("Kesalahan integritas data: ${e.message}", e)
        }
    }

    private fun isDuplicateKey(message: String, sqlState: String?): Boolean =
        message.contains("duplicate") ||
                message.contains("unique") ||
                sqlState == "23505" || // PostgreSQL
                sqlState == "23000"    // MySQL

    private fun isForeignKeyViolation(message: String, sqlState: String?): Boolean =
        message.contains("foreign key") ||
                message.contains("violates foreign key") ||
                message.contains("constraint") && message.contains("reference") ||
                sqlState == "23503" || // PostgreSQL
                sqlState == "23000"    // MySQL (also covers FK)

    private fun isNotNullViolation(message: String, sqlState: String?): Boolean =
        message.contains("not null") ||
                message.contains("null value") ||
                sqlState == "23502" // PostgreSQL

    private fun isCheckConstraintViolation(message: String, sqlState: String?): Boolean =
        message.contains("check constraint") ||
                sqlState == "23514" // PostgreSQL

    private fun isConnectionError(message: String, sqlState: String?): Boolean =
        message.contains("connection") ||
                message.contains("communications link failure") ||
                sqlState?.startsWith("08") == true // Connection exceptions

    private fun isTimeoutError(message: String, sqlState: String?): Boolean =
        message.contains("timeout") ||
                message.contains("lock") && message.contains("timeout") ||
                sqlState == "40001" || // Serialization failure
                sqlState == "57014"    // Query canceled

    private fun isSyntaxError(message: String, sqlState: String?): Boolean =
        message.contains("syntax error") ||
                message.contains("sql syntax") ||
                sqlState?.startsWith("42") == true // Syntax errors
}

// Specific exceptions
class NotNullViolationException(message: String, cause: Throwable? = null) :
    RepositoryException(message, cause)

class CheckConstraintException(message: String, cause: Throwable? = null) :
    RepositoryException(message, cause)

class DatabaseConnectionException(message: String, cause: Throwable? = null) :
    RepositoryException(message, cause)

class DatabaseTimeoutException(message: String, cause: Throwable? = null) :
    RepositoryException(message, cause)

class DatabaseSyntaxException(message: String, cause: Throwable? = null) :
    RepositoryException(message, cause)