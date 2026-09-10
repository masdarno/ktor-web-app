package id.darno.module.auth.repository

import id.darno.core.database.DatabaseQuery
import id.darno.module.auth.database.table.PasswordResetTokenTable
import id.darno.module.auth.model.PasswordResetToken
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class PasswordResetRepositoryImpl(
    private val databaseQuery: DatabaseQuery
) : PasswordResetRepository {

    override suspend fun save(
        tokenHash: String,
        userId: Short,
        expiresAt: LocalDateTime
    ){
        databaseQuery {
            PasswordResetTokenTable.insert {
                it[PasswordResetTokenTable.token] = tokenHash
                it[PasswordResetTokenTable.userId] = userId
                it[PasswordResetTokenTable.expiresAt] = expiresAt
                it[usedAt] = null
            }
        }
    }

    override suspend fun findValid(tokenHash: String): PasswordResetToken? = databaseQuery {
        PasswordResetTokenTable
            .selectAll()
            .where {
                (PasswordResetTokenTable.token eq tokenHash) and
                (PasswordResetTokenTable.usedAt.isNull()) and
                (PasswordResetTokenTable.expiresAt greater Clock.System.now().toLocalDateTime(TimeZone.UTC))
            }
            .map(::toDomain)
            .singleOrNull()
    }

    override suspend fun markUsed(tokenHash: String) {
        val usedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        databaseQuery {
            PasswordResetTokenTable.update(
                { PasswordResetTokenTable.token eq tokenHash }
            ) {
                it[this.usedAt] = usedAt
            }
        }
    }

    override suspend fun deleteByUserId(userId: Short) {
        databaseQuery {
            PasswordResetTokenTable.deleteWhere {
                PasswordResetTokenTable.userId eq userId
            }
        }
    }

    private fun toDomain(row: ResultRow) = PasswordResetToken(
        tokenHash = row[PasswordResetTokenTable.token],
        userId = row[PasswordResetTokenTable.userId].value,
        expiresAt = row[PasswordResetTokenTable.expiresAt]
    )
}
