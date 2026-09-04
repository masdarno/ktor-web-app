package id.darno.module.auth.mapper

import id.darno.core.session.model.TempUserSession
import id.darno.core.session.model.UserSession
import id.darno.module.unit.domain.UnitDomain
import id.darno.module.user.domain.UserDomain

fun UserDomain.combineWith(unit: UnitDomain): UserSession {
    return UserSession(
        userId = id,
        nama = nama,
        photoUrl = photoUrl,
        roleId = roleId,
        role = role,
        unitId = unit.id,
        unit = unit.nama
    )
}

fun TempUserSession.combineWith(unit: UnitDomain): UserSession {
    return UserSession(
        userId = userId,
        nama = nama,
        photoUrl = photoUrl,
        roleId = roleId,
        role = role,
        unitId = unit.id,
        unit = unit.nama
    )
}

fun TempUserSession.toUserSession(): UserSession {
    return UserSession(
        userId = userId,
        nama = nama,
        photoUrl = photoUrl,
        roleId = roleId,
        role = role,
        unitId = unitId!!,
        unit = unit!!
    )
}