package id.darno.module.role.repository

import id.darno.core.database.dbQuery
import id.darno.module.menu.database.table.RoleMenuTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

class RoleMenuRepositoryImpl: RoleMenuRepository {

    override suspend fun findAllRoleMenus(): Map<Short, Set<Short>> =
        dbQuery {
            RoleMenuTable
                .selectAll()
                .groupBy(
                    keySelector = { it[RoleMenuTable.roleId].value },
                    valueTransform = { it[RoleMenuTable.menuId].value }
                )
                .mapValues { (_, menuIds) ->
                    menuIds.toSet()
                }
        }

    override suspend fun findMenuIdsByRole(
        roleId: Short
    ): Set<Short> =
        dbQuery {
            RoleMenuTable
                .selectAll()
                .where { RoleMenuTable.roleId eq roleId }   // ✅ BENAR
                .map { it[RoleMenuTable.menuId].value }
                .toSet()
        }

    override suspend fun replaceRoleMenus(
        roleId: Short,
        menuIds: Set<Short>
    ): Unit =
        dbQuery {
            RoleMenuTable.deleteWhere {
                RoleMenuTable.roleId eq roleId
            }

            menuIds.forEach { menuId ->
                RoleMenuTable.insert {
                    it[this.roleId] = roleId
                    it[this.menuId] = menuId
                }
            }
        }

    override suspend fun addMenuToRole(
        roleId: Short,
        menuId: Short
    ): Unit =
        dbQuery {
            RoleMenuTable.insertIgnore {
                it[this.roleId] = roleId
                it[this.menuId] = menuId
            }
        }

    override suspend fun removeMenuFromRole(
        roleId: Short,
        menuId: Short
    ): Unit =
        dbQuery {
            RoleMenuTable.deleteWhere {
                (RoleMenuTable.roleId eq roleId) and
                        (RoleMenuTable.menuId eq menuId)
            }
        }
}