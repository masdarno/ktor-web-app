package id.darno.module.menu

import id.darno.core.database.DatabaseQuery
import id.darno.module.menu.repository.MenuRepository
import id.darno.module.menu.repository.MenuRepositoryImpl
import id.darno.module.menu.service.MenuCacheService
import id.darno.module.menu.service.MenuCacheServiceImpl
import id.darno.module.menu.service.MenuService
import id.darno.module.menu.service.MenuServiceImpl
import id.darno.module.role.service.RoleMenuCacheService
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import io.ktor.server.plugins.di.resolve

fun Application.configureMenuDependencies(){
    dependencies {
        provide<MenuRepository> {
            MenuRepositoryImpl(resolve<DatabaseQuery>())
        }
        provide<MenuCacheService>{
            MenuCacheServiceImpl(resolve<MenuRepository>())
        }
        provide<MenuService>{
            MenuServiceImpl(
                resolve<MenuCacheService>(),
                resolve<RoleMenuCacheService>()
            )
        }
    }
}