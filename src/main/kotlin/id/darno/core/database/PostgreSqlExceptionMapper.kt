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

class PostgreSqlExceptionMapper : DbExceptionMapper {

    override fun map(exception: ExposedSQLException): RepositoryException {
        val info = DbExceptionUtils.inspect(exception)

        val constraint =
            DbExceptionUtils.extractConstraint(info.message)

        return when (info.sqlState) {

            // Integrity
            "23505" ->
                DuplicateKeyException(
                    constraint = constraint,
                    cause = exception
                )

            "23503" ->
                ForeignKeyException(
                    constraint = constraint,
                    cause = exception
                )

            "23502" ->
                NotNullViolationException(
                    constraint = constraint,
                    cause = exception
                )

            "23514" ->
                CheckConstraintException(
                    constraint = constraint,
                    cause = exception
                )

            // Concurrency
            "40001",
            "40P01" ->
                DatabaseConcurrencyException(
                    cause = exception
                )

            // Timeout / cancellation
            "57014" ->
                DatabaseTimeoutException(
                    cause = exception
                )

            else -> when {

                // Connection failure
                info.sqlState?.startsWith("08") == true ->
                    DatabaseConnectionException(
                        cause = exception
                    )

                // SQL syntax / access rule
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
}