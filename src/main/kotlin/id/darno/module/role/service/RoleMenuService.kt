package id.darno.module.role.service

interface RoleMenuService {
    suspend fun updateRoleMenus(roleId: Short, menuIds: Set<Short>)
    suspend fun addMenuToRole(roleId: Short, menuId: Short)
    suspend fun removeMenuFromRole(roleId: Short, menuId: Short)
}