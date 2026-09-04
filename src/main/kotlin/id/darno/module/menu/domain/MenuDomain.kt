package id.darno.module.menu.domain

data class MenuDomain(
    val id: Short,
    val parentId: Short?,
    val type: MenuType,
    val name: String?,
    val url: String?,
    val icon: String?,
    val badgeText: String?,
    val badgeColor: String?,
    val urut: Int,
    val permissionName: String?
)