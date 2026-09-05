package id.darno.module.role

import id.darno.module.menu.service.MenuAccessService
import id.darno.module.menu.service.MenuAccessServiceImpl
import id.darno.module.menu.service.MenuCacheService
import id.darno.module.role.repository.RoleMenuRepository
import id.darno.module.role.repository.RoleMenuRepositoryImpl
import id.darno.module.role.repository.RoleRepository
import id.darno.module.role.repository.RoleRepositoryImpl
import id.darno.module.role.service.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

fun Application.configureRoleDependencies(){
    dependencies {
        provide<RoleRepository> { RoleRepositoryImpl() }
        provide<RoleService> {
            RoleServiceImpl(resolve<RoleRepository>())
        }
        provide<RoleMenuRepository>{ RoleMenuRepositoryImpl() }
        provide<RoleMenuCacheService> {
            RoleMenuCacheServiceImpl(resolve<RoleMenuRepository>())
        }
        provide<RoleMenuService> {
            RoleMenuServiceImpl(
                resolve<RoleMenuRepository>(),
                resolve<RoleMenuCacheService>()
            )
        }
        provide<MenuAccessService> {
            MenuAccessServiceImpl(
                resolve<MenuCacheService>(),
                resolve<RoleMenuCacheService>()
            )
        }
    }
}