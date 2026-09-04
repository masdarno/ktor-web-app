package id.darno.module.auth.model

import id.darno.module.unit.domain.UnitDomain
import id.darno.module.user.domain.UserDomain

sealed class AuthResult {
    data class Success(val user: UserDomain, val unit: List<UnitDomain>) : AuthResult()
    object InvalidCredentials : AuthResult()
    object UserInactive : AuthResult()
}