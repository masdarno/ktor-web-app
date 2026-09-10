package id.darno.module.menu.repository

import id.darno.core.database.DatabaseQuery
import id.darno.module.menu.database.table.MenuTable
import id.darno.module.menu.domain.MenuDomain
import id.darno.module.menu.mapper.MenuMapper
import org.jetbrains.exposed.v1.jdbc.*

class MenuRepositoryImpl(
    private val databaseQuery: DatabaseQuery
) : MenuRepository{
    override suspend fun findAllMenus(): List<MenuDomain> =
        databaseQuery {
            MenuTable
                .selectAll()
                .map(MenuMapper::toDomain)
        }
}