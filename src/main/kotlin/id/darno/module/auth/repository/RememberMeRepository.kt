package id.darno.module.auth.repository

import id.darno.module.user.model.RememberToken

interface RememberMeRepository {
    suspend fun findBySelector(selector: String): RememberToken?
    suspend fun save(token: RememberToken)
    suspend fun delete(selector: String)
    suspend fun deleteByUserId(userId: Short)
}