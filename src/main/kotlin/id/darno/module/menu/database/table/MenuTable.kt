package id.darno.module.menu.database.table

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.ReferenceOption

// Menus Table
object MenuTable: IdTable<Short>("menus") {
    override val id: Column<EntityID<Short>> = short("id").autoIncrement().entityId()

    val parentId = reference("parent_id", MenuTable, onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE).nullable()
    val type = varchar("type", 10).clientDefault { "item" } // 'item', 'title', 'group', 'divider'
    val name = varchar("name", 255).nullable()
    val url = varchar("url", 255).nullable()
    val icon = varchar("icon", 255).nullable()
    val badgeText = varchar("badge_text", 255).nullable()
    val badgeColor = varchar("badge_color", 255).nullable()
    val urut = integer("urut").default(0)
    val permissionName = varchar("permission_name", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

