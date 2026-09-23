package id.darno.module.menu.service

import id.darno.module.role.service.RoleMenuCacheService

class MenuAccessServiceImpl(
    private val menuCacheService: MenuCacheService,
    private val roleMenuCacheService: RoleMenuCacheService
) : MenuAccessService {

    override suspend fun resolveMenuIdByPath(path: String): Short? {
        val normalizedPath = "/" + path.trim('/')

        return menuCacheService.getAllMenus()
            .filter { !it.url.isNullOrBlank() && !it.url.startsWith("http") }
            .filter { normalizedPath == it.url || normalizedPath.startsWith("${it.url}/") }
            .maxByOrNull { it.url!!.length } // prefix terpanjang menang
            ?.id
    }

    override suspend fun hasAccess(roleId: Short, path: String): Boolean {
        val menuId = resolveMenuIdByPath(path) ?: return true // path tidak terdaftar sbg menu => tidak diproteksi
        return menuId in roleMenuCacheService.getMenuIdsByRole(roleId)
    }
}