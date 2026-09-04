package id.darno.module.menu.service

import id.darno.module.menu.domain.MenuNode

interface MenuService {
    suspend fun buildSidebarMenu(roleId: Short): List<MenuNode>
}