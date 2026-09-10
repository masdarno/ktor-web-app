package id.darno.core.database

import id.darno.core.exceptions.repository.RepositoryException
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException

interface DbExceptionMapper {

    fun map(exception: ExposedSQLException): RepositoryException
}