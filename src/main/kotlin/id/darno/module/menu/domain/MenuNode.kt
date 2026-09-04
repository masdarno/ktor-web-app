package id.darno.module.menu.domain

// Untuk view
data class MenuNode(
    val id: Short,
    val type: MenuType,
    val name: String?,
    val url: String?,
    val icon: String?,
    val badgeText: String?,
    val badgeColor: String?,
    val urut: Int,
    val isExternalLink: Boolean,
    val children: List<MenuNode>
)