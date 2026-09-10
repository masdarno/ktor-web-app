package id.darno.core.database

import id.darno.core.exceptions.repository.CheckConstraintException
import id.darno.core.exceptions.repository.DatabaseConcurrencyException
import id.darno.core.exceptions.repository.DatabaseConnectionException
import id.darno.core.exceptions.repository.DatabaseSyntaxException
import id.darno.core.exceptions.repository.DatabaseTimeoutException
import id.darno.core.exceptions.repository.DuplicateKeyException
import id.darno.core.exceptions.repository.ForeignKeyException
import id.darno.core.exceptions.repository.NotNullViolationException
import id.darno.core.exceptions.repository.RepositoryException
import id.darno.core.exceptions.repository.UnknownDatabaseException
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException

class MySqlExceptionMapper : DbExceptionMapper {

    override fun map(exception: ExposedSQLException): RepositoryException {
        val info = DbExceptionUtils.inspect(exception)

        val constraint =
            DbExceptionUtils.extractConstraint(info.message)

        return when {

            // ---------------------------------------------------------
            // Data integrity
            // ---------------------------------------------------------

            // Duplicate entry
            // MySQL: 1062
            info.errorCode == 1062 ->
                DuplicateKeyException(
                    constraint = constraint,
                    cause = exception
                )

            // Cannot delete/update parent row
            // MySQL: 1451
            info.errorCode == 1451 ->
                ForeignKeyException(
                    constraint = constraint,
                    cause = exception
                )

            // Cannot add/update child row
            // MySQL: 1452
            info.errorCode == 1452 ->
                ForeignKeyException(
                    constraint = constraint,
                    cause = exception
                )

            // Column cannot be null
            // MySQL: 1048
            info.errorCode == 1048 ->
                NotNullViolationException(
                    constraint = constraint,
                    cause = exception
                )

            // CHECK constraint violation
            // MySQL: 3819
            info.errorCode == 3819 ||
                    info.message.contains("check constraint") ->
                CheckConstraintException(
                    constraint = constraint,
                    cause = exception
                )

            // ---------------------------------------------------------
            // Connection
            // ---------------------------------------------------------

            // SQLSTATE class 08 = connection exception
            info.sqlState?.startsWith("08") == true ->
                DatabaseConnectionException(
                    cause = exception
                )

            // ---------------------------------------------------------
            // Timeout
            // ---------------------------------------------------------

            // Lock wait timeout exceeded
            // MySQL: 1205
            info.errorCode == 1205 ->
                DatabaseTimeoutException(
                    cause = exception
                )

            // ---------------------------------------------------------
            // Concurrency
            // ---------------------------------------------------------

            // Deadlock found when trying to get lock
            // MySQL: 1213
            info.errorCode == 1213 ->
                DatabaseConcurrencyException(
                    cause = exception
                )

            // Serialization failure
            info.sqlState == "40001" ->
                DatabaseConcurrencyException(
                    cause = exception
                )

            // ---------------------------------------------------------
            // SQL / syntax
            // ---------------------------------------------------------

            // SQL syntax error
            // MySQL: 1064
            info.errorCode == 1064 ||
                    info.sqlState?.startsWith("42") == true ->
                DatabaseSyntaxException(
                    cause = exception
                )

            // ---------------------------------------------------------
            // Unknown
            // ---------------------------------------------------------

            else ->
                UnknownDatabaseException(
                    cause = exception
                )
        }
    }
}