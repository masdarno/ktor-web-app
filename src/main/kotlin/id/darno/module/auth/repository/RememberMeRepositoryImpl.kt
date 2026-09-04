package id.darno.module.auth.repository

import id.darno.core.database.dbQuery
import id.darno.module.auth.database.table.RememberMeTokenTable
import id.darno.module.user.model.RememberToken
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

class RememberMeRepositoryImpl : RememberMeRepository {

    override suspend fun findBySelector(selector: String): RememberToken? =
        dbQuery {
            RememberMeTokenTable
                .selectAll()
                .where { RememberMeTokenTable.selector eq selector }
                .map {
                    RememberToken(
                        selector = it[RememberMeTokenTable.selector],
                        userId = it[RememberMeTokenTable.userId].value,
                        unitId = it[RememberMeTokenTable.unitId].value,
                        validatorHash = it[RememberMeTokenTable.validatorHash],
                        expiresAt = it[RememberMeTokenTable.expiresAt]
                    )
                }
                .singleOrNull()
        }

    override suspend fun save(token: RememberToken) {
        dbQuery {
            RememberMeTokenTable.insert {
                it[selector] = token.selector
                it[userId] = token.userId
                it[unitId] = token.unitId
                it[validatorHash] = token.validatorHash
                it[expiresAt] = token.expiresAt
            }
        }
    }

    override suspend fun delete(selector: String) {
        dbQuery {
            RememberMeTokenTable.deleteWhere {
                RememberMeTokenTable.selector eq selector
            }
        }
    }

    override suspend fun deleteByUserId(userId: Short) {
        dbQuery {
            RememberMeTokenTable.deleteWhere{
                RememberMeTokenTable.userId eq userId
            }
        }
    }
}