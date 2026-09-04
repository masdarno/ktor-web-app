package id.darno.module.menu.service

import id.darno.module.menu.domain.MenuDomain
import id.darno.module.menu.domain.MenuNode
import id.darno.module.menu.domain.MenuType
import id.darno.module.role.service.RoleMenuCacheService

class MenuServiceImpl(
    private val menuCacheService: MenuCacheService,
    private val roleMenuCacheService: RoleMenuCacheService
) : MenuService {

    override suspend fun buildSidebarMenu(roleId: Short): List<MenuNode> {
        val allowedIds = roleMenuCacheService.getMenuIdsByRole(roleId)
        val allMenus = menuCacheService.getAllMenus()

        val visibleMenus = allMenus.filter { menu ->
            when (menu.type) {
                MenuType.ITEM, MenuType.GROUP -> menu.id in allowedIds
                MenuType.TITLE, MenuType.DIVIDER -> true
            }
        }

        val byParent = visibleMenus.groupBy { it.parentId }

        return buildChildren(null, byParent)
            .filterNot { it.type == MenuType.GROUP && it.children.isEmpty() }
            .sortedBy { it.urut }
    }

    private fun buildChildren(
        parentId: Short?,
        byParent: Map<Short?, List<MenuDomain>>
    ): List<MenuNode> =
        byParent[parentId]
            ?.sortedBy { it.urut }
            ?.map { menu ->
                MenuNode(
                    id = menu.id,
                    type = menu.type,
                    nama = menu.nama,
                    url = menu.url,
                    icon = menu.icon,
                    badgeText = menu.badgeText,
                    badgeColor = menu.badgeColor,
                    urut = menu.urut,
                    isExternalLink = menu.url?.startsWith("http") == true,
                    children = buildChildren(menu.id, byParent)
                )
            }
            ?: emptyList()
}