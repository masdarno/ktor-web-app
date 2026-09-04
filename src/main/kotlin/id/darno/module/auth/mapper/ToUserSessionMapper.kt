package id.darno.module.auth.mapper

import id.darno.core.session.model.TempUserSession
import id.darno.core.session.model.UserSession
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.user.domain.UserDomain

fun UserDomain.combineWith(unit: UnitDomain): UserSession {
    return UserSession(
        userId = id,
        name = name,
        photoUrl = photoUrl,
        roleId = roleId,
        role = role,
        unitId = unit.id,
        unit = unit.name
    )
}

fun TempUserSession.combineWith(unit: UnitDomain): UserSession {
    return UserSession(
        userId = userId,
        name = name,
        photoUrl = photoUrl,
        roleId = roleId,
        role = role,
        unitId = unit.id,
        unit = unit.name
    )
}

fun TempUserSession.toUserSession(): UserSession {
    return UserSession(
        userId = userId,
        name = name,
        photoUrl = photoUrl,
        roleId = roleId,
        role = role,
        unitId = unitId!!,
        unit = unit!!
    )
}