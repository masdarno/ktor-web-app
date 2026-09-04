package id.darno.module.role.mapper

import id.darno.module.role.database.dao.RoleEntity
import id.darno.module.role.domain.RoleDomain

fun RoleEntity.toRoleDomain(): RoleDomain{
    return RoleDomain(
        id = this.id.value,
        nama = this.nama
    )
}