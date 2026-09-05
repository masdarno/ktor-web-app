package id.darno.module.user.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.pageddata.helper.pagedQueryParameters
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.module.unit.service.UnitService
import id.darno.module.user.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

class UserUnitController(private val userService: UserService, private val unitService: UnitService) {

    private val logger = LoggerFactory.getLogger(UserUnitController::class.java)

    companion object {
        private const val TEMPLATE_PAGE = "pages/user/user-unit.html"
        private const val TEMPLATE_TABLE = "pages/user/fragments/user-unit-table.html"
        private const val TEMPLATE_FORM = "pages/user/fragments/user-unit-form.html"
        private const val PAGE_TITLE = "Daftar Pengguna Unit"
    }

    suspend fun index(call: ApplicationCall) {

        val query = call.pagedQueryParameters()

        val units = unitService.getAll()

        /*
         * Unit default:
         * gunakan unit pertama jika belum ada unitId
         * dari URL.
         */
        val requestedUnitId =
            call.request.queryParameters["unitId"]
                ?.toShortOrNull()

        val selectedUnitId: Short =
            requestedUnitId
                ?.takeIf { id -> units.any { it.id == id } }
                ?: units.first().id

        val result = userService.getUserUnitTable(
            query = query,
            unitId = selectedUnitId
        )

        call.respondPebblePage(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "result" to result,
                "params" to query,
                "units" to units,
                "selectedUnitId" to selectedUnitId
            )
        )
    }

    suspend fun table(call: ApplicationCall) {

        val query = call.pagedQueryParameters()

        val unitId = call.request.queryParameters["unitId"]
            ?.toShortOrNull()
            ?: throw BadRequestException("unitId wajib diisi")

        val result = userService.getUserUnitTable(
            query = query,
            unitId = unitId
        )

        call.respond(
            PebbleContent(
                TEMPLATE_TABLE,
                mapOf(
                    "result" to result,
                    "params" to query
                )
            )
        )
    }

    suspend fun delete(call: ApplicationCall, userId: Short, unitId: Short) {
        try {
            userService.deleteUserUnit(userId, unitId)
            call.hxTriggerWithToast(
                "User Unit BERHASIL dihapus.",
                ToastType.SUCCESS,
                "user-unit-deleted")
            call.respond(HttpStatusCode.NoContent)
        } catch (e: ApplicationException) {
            logger.error("Failed to delete user (id: {})", userId, e)

            call.hxTriggerWithToast(
                "User Unit GAGAL dihapus.",
                ToastType.ERROR)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}