package id.darno.module.auth.service

import id.darno.core.session.model.RememberMeCookie
import id.darno.core.session.model.UserSession
import id.darno.module.user.model.RememberToken

interface RememberMeService {

    suspend fun authenticate(
        cookie: RememberMeCookie
    ): Result<UserSession>

    suspend fun issueToken(
        userId: Short,
        unitId: Short
    ): Pair<RememberToken, RememberMeCookie>

    suspend fun save(token: RememberToken)

    suspend fun revoke(selector: String)

    suspend fun revokeByUserId(id: Short)
}