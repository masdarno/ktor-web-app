package id.darno.module.role.service

import id.darno.module.role.repository.RoleMenuRepository
import org.slf4j.LoggerFactory

class RoleMenuServiceImpl(
    private val roleMenuRepository: RoleMenuRepository,
    private val roleMenuCacheService: RoleMenuCacheService
) : RoleMenuService {

    private val logger = LoggerFactory.getLogger(RoleMenuServiceImpl::class.java)

    override suspend fun updateRoleMenus(roleId: Short, menuIds: Set<Short>) {
        roleMenuRepository.replaceRoleMenus(roleId, menuIds)
        roleMenuCacheService.reload(roleId)
        logger.info("Role menus replaced for roleId={}", roleId)
    }

    override suspend fun addMenuToRole(roleId: Short, menuId: Short) {
        roleMenuRepository.addMenuToRole(roleId, menuId)
        roleMenuCacheService.reload(roleId)
        logger.info("Menu {} added to role {}", menuId, roleId)
    }

    override suspend fun removeMenuFromRole(roleId: Short, menuId: Short) {
        roleMenuRepository.removeMenuFromRole(roleId, menuId)
        roleMenuCacheService.reload(roleId)
        logger.info("Menu {} removed from role {}", menuId, roleId)
    }
}