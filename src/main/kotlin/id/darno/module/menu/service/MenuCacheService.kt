package id.darno.module.menu.service

import id.darno.module.menu.domain.MenuDomain

interface MenuCacheService {
    suspend fun init()
    suspend fun getAllMenus(): Collection<MenuDomain>
    suspend fun reload()
}