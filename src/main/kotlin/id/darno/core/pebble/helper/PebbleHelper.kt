package id.darno.core.pebble.helper

import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.session.model.UserSession
import id.darno.module.menu.service.MenuService
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.plugins.di.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("PEBBLE_HELPER")

suspend fun ApplicationCall.respondPebblePage(
    template: String,
    model: Map<String, Any> = emptyMap()
) {
    val enrichedModel = model.toMutableMap().apply {
        // Injek data UserSession
        val currentRoleId: Short? = sessions.get<UserSession>()?.let {
            put("userSession", it)
            it.roleId // return roleId
        }

        logger.debug("Build SidebarMenu with roleId = $currentRoleId")
        // Build menu jika roleId tersedia
        currentRoleId?.let { roleId ->
            //menuBuilder menggunakan dependency injection
            val menuService: MenuService by application.dependencies
            val sidebarMenus = menuService.buildSidebarMenu(roleId)
            logger.debug("Sidebar menu size = ${sidebarMenus.size}")

            put("menuData", sidebarMenus)
        }
        // CSRF Token
        val csrfToken = ensureCsrfToken()
        put("csrfToken", csrfToken)
    }
    respond(PebbleContent(template, enrichedModel))
}