package id.darno.core.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class DatabaseQuery(
    private val dbExceptionMapper: DbExceptionMapper
) {

    suspend operator fun <T> invoke(
        block: suspend () -> T
    ): T =
        try {
            withContext(Dispatchers.IO) {
                suspendTransaction {
                    block()
                }
            }
        } catch (e: ExposedSQLException) {
            throw dbExceptionMapper.map(e)
        }
}