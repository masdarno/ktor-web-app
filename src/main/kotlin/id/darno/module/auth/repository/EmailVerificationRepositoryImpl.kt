package id.darno.module.auth.repository

import id.darno.core.database.dbQuery
import id.darno.module.auth.database.table.EmailVerificationTokenTable
import id.darno.module.auth.model.EmailVerificationToken
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.time.Duration.Companion.hours

class EmailVerificationRepositoryImpl :
    EmailVerificationRepository {

    override suspend fun find(token: String): EmailVerificationToken? =
        dbQuery {
            EmailVerificationTokenTable
                .selectAll()
                .where {
                    (EmailVerificationTokenTable.token eq token) and
                            (EmailVerificationTokenTable.expiresAt greater Clock.System.now()
                                .toLocalDateTime(TimeZone.UTC))
                }
                .map {
                    EmailVerificationToken(
                        token = it[EmailVerificationTokenTable.token],
                        userId = it[EmailVerificationTokenTable.userId].value,
                        expiresAt = it[EmailVerificationTokenTable.expiresAt]
                    )
                }
                .singleOrNull()
        }

    override suspend fun save(
        token: String,
        userId: Short
    ) {
        val expiresAt =
            Clock.System.now()
                .plus(24.hours)
                .toLocalDateTime(TimeZone.UTC)

        dbQuery {
            // 1 user = 1 active token
            EmailVerificationTokenTable.deleteWhere {
                EmailVerificationTokenTable.userId eq userId
            }

            EmailVerificationTokenTable.insert {
                it[this.token] = token
                it[this.userId] = userId
                it[this.expiresAt] = expiresAt
            }
        }
    }

    override suspend fun delete(token: String) {
        dbQuery {
            EmailVerificationTokenTable.deleteWhere {
                EmailVerificationTokenTable.token eq token
            }
        }
    }

    override suspend fun deleteByUserId(userId: Short) {
        dbQuery {
            EmailVerificationTokenTable.deleteWhere {
                EmailVerificationTokenTable.userId eq userId
            }
        }
    }
}
