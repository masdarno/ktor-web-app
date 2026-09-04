package id.darno.module.menu.mapper

import id.darno.module.menu.database.table.MenuTable
import id.darno.module.menu.domain.MenuDomain
import id.darno.module.menu.domain.MenuType
import org.jetbrains.exposed.v1.core.*

object MenuMapper {
    fun toDomain(row: ResultRow): MenuDomain =
        MenuDomain(
            id = row[MenuTable.id].value,
            parentId = row[MenuTable.parentId]?.value,
            type = MenuType.valueOf(row[MenuTable.type].uppercase()),
            nama = row[MenuTable.nama],
            url = row[MenuTable.url],
            icon = row[MenuTable.icon],
            badgeText = row[MenuTable.badgeText],
            badgeColor = row[MenuTable.badgeColor],
            urut = row[MenuTable.urut],
            permissionName = row[MenuTable.permissionName]
        )
}