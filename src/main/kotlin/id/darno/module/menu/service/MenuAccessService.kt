package id.darno.module.menu.service

interface MenuAccessService {
    suspend fun resolveMenuIdByPath(path: String): Short?
    suspend fun hasAccess(roleId: Short, path: String): Boolean
}