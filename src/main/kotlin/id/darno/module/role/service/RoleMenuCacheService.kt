package id.darno.module.role.service

interface RoleMenuCacheService {
    suspend fun init()
    suspend fun getMenuIdsByRole(roleId: Short): Set<Short>
    suspend fun reload(roleId: Short? = null)
}