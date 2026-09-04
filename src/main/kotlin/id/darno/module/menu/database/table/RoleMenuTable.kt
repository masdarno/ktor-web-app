package id.darno.module.menu.database.table

import id.darno.module.role.database.table.RoleTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.ReferenceOption

// RoleMenus Join Table
object RoleMenuTable : Table("role_menus") {
    val roleId = reference("role_id", RoleTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val menuId = reference("menu_id", MenuTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(roleId, menuId, name = "PK_RoleMenus")
}