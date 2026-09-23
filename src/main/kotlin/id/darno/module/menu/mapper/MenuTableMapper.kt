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
            url = normalizeUrl(row[MenuTable.url]),
            icon = row[MenuTable.icon],
            badgeText = row[MenuTable.badgeText],
            badgeColor = row[MenuTable.badgeColor],
            urut = row[MenuTable.urut],
            permissionName = row[MenuTable.permissionName]
        )
    /**
     * Normalisasi url menu agar selalu absolute path (diawali '/'),
     * tanpa trailing slash, kecuali url eksternal (http/https) yang dibiarkan apa adanya.
     */
    private fun normalizeUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        if (url.startsWith("http")) return url
        return "/" + url.trim('/')
    }
}