package id.darno.core.database.exception.mapper

import id.darno.core.database.exception.DbExceptionMapper
import id.darno.core.database.exception.DbExceptionUtils
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

class MariaDbExceptionMapper : DbExceptionMapper {

    override fun map(exception: ExposedSQLException): RepositoryException {
        val info = DbExceptionUtils.inspect(exception)

        val constraint =
            DbExceptionUtils.extractConstraint(info.message)

        return when {

            // Duplicate key / unique constraint
            info.errorCode == 1062 ->
                DuplicateKeyException(
                    constraint = constraint,
                    cause = exception
                )

            // Cannot delete/update parent row
            info.errorCode == 1451 ->
                ForeignKeyException(
                    constraint = constraint,
                    cause = exception
                )

            // Cannot insert/update child row
            info.errorCode == 1452 ->
                ForeignKeyException(
                    constraint = constraint,
                    cause = exception
                )

            // Column cannot be null
            info.errorCode == 1048 ->
                NotNullViolationException(
                    constraint = constraint,
                    cause = exception
                )

            // CHECK constraint
            info.errorCode == 4025 ||
                    info.message.contains("check constraint") ->
                CheckConstraintException(
                    constraint = constraint,
                    cause = exception
                )

            // Connection errors
            info.sqlState?.startsWith("08") == true ->
                DatabaseConnectionException(
                    cause = exception
                )

            // Lock wait timeout
            info.errorCode == 1205 ->
                DatabaseTimeoutException(
                    cause = exception
                )

            // Deadlock
            info.errorCode == 1213 ->
                DatabaseConcurrencyException(
                    cause = exception
                )

            // SQL syntax / invalid SQL
            info.errorCode == 1064 ||
                    info.sqlState?.startsWith("42") == true ->
                DatabaseSyntaxException(
                    cause = exception
                )

            else ->
                UnknownDatabaseException(
                    cause = exception
                )
        }
    }
}