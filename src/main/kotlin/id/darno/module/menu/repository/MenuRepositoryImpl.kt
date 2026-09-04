package id.darno.module.menu.repository

import id.darno.core.database.dbQuery
import id.darno.module.menu.database.table.MenuTable
import id.darno.module.menu.domain.MenuDomain
import id.darno.module.menu.mapper.MenuMapper
import org.jetbrains.exposed.v1.jdbc.*

class MenuRepositoryImpl : MenuRepository{
    override suspend fun findAllMenus(): List<MenuDomain> =
        dbQuery {
            MenuTable
                .selectAll()
                .map(MenuMapper::toDomain)
        }
}