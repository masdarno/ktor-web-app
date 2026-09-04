package id.darno.module.auth.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxRedirectTo
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.session.helper.ensureCsrfToken
import id.darno.core.session.helper.regenerateCsrfToken
import id.darno.core.session.model.TempUserSession
import id.darno.module.auth.mapper.combineWith
import id.darno.module.auth.service.RememberMeService
import id.darno.module.unit.service.UnitService
import id.darno.module.user.service.UserLookupService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.pebble.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.slf4j.LoggerFactory

class SelectUnitController(
    private val userLookupService: UserLookupService,
    private val unitService: UnitService,
    private val rememberMeService: RememberMeService) {

    private val logger = LoggerFactory.getLogger(SelectUnitController::class.java)

    companion object {
        private const val TEMPLATE_PAGE = "pages/auth/select-unit.html"
        private const val PAGE_TITLE = "Select Unit"
    }

    suspend fun index(call: ApplicationCall){
        val session = call.sessions.get<TempUserSession>()!!

        logger.info("Select unit user with username: {}", session.name)

        val units = userLookupService.getUnitsForUser(session.userId)
        val csrfToken = call.ensureCsrfToken()
        call.respond(PebbleContent(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "units" to units,
                "tempUserSession" to session,
                "csrfToken" to csrfToken
            )
        ))
    }

    suspend fun selectUnit(call: ApplicationCall) {
        val tempSession = call.principal<TempUserSession>()
        val unitId = call.receiveParameters()["unitId"]?.toShortOrNull()

        // Validasi input
        if (unitId == null) {
            call.hxTriggerWithToast(
                "Unit ID tidak valid",
                ToastType.ERROR
            )
            return
        }

        // Validasi session
        if (tempSession == null) {
            call.hxRedirectTo("/login")
            return
        }

        // Proses pemilihan unit
        try {
            val unit = unitService.getById(unitId)
            val userSession = tempSession.combineWith(unit)

            call.sessions.set(userSession)
            call.sessions.clear<TempUserSession>()

            // If Remember me
            if (tempSession.rememberMe) {
                val (token, cookie) = rememberMeService.issueToken(userSession.userId, unitId)
                rememberMeService.save(token)
                call.sessions.set(cookie)
            }

            // Regenerate CSRF Token
            call.regenerateCsrfToken()

            call.hxRedirectTo("/dashboard")
        } catch (e: ApplicationException) {
            call.hxTriggerWithToast(
                e.message ?: "Unit tidak ditemukan",
                ToastType.ERROR
            )
        }
    }
}