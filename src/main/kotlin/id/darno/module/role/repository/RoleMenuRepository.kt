package id.darno.module.role.repository

interface RoleMenuRepository {

    /**
     * Ambil seluruh mapping role -> menuIds
     * Dipakai untuk preload cache
     */
    suspend fun findAllRoleMenus(): Map<Short, Set<Short>>

    /**
     * Ambil semua menuId milik satu role
     */
    suspend fun findMenuIdsByRole(
        roleId: Short
    ): Set<Short>

    /**
     * Replace seluruh menu milik role
     */
    suspend fun replaceRoleMenus(
        roleId: Short,
        menuIds: Set<Short>
    )

    /**
     * Tambah 1 menu ke role
     */
    suspend fun addMenuToRole(
        roleId: Short,
        menuId: Short
    )

    /**
     * Hapus 1 menu dari role
     */
    suspend fun removeMenuFromRole(
        roleId: Short,
        menuId: Short
    )
}