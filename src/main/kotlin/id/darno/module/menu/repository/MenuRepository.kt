package id.darno.module.menu.repository

import id.darno.module.menu.domain.MenuDomain

interface MenuRepository {
    suspend fun findAllMenus(): List<MenuDomain>
}