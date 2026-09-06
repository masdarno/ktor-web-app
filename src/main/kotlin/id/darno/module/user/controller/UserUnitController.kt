package id.darno.module.user.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.pageddata.helper.pagedQueryParameters
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.module.unit.service.UnitService
import id.darno.module.user.service.UserUnitService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

class UserUnitController(private val userUnitService: UserUnitService, private val unitService: UnitService) {

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

        val requestedUnitId =
            call.request.queryParameters["unitId"]
                ?.toShortOrNull()

        val selectedUnitId: Short =
            requestedUnitId
                ?.takeIf { id -> units.any { it.id == id } }
                ?: units.first().id

        val result = userUnitService.getUserUnitTable(
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

        val result = userUnitService.getUserUnitTable(
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

    // Menampilkan form berisi checkbox user yang BELUM terdaftar di unit terpilih
    suspend fun form(call: ApplicationCall) {

        val unitId = call.request.queryParameters["unitId"]
            ?.toShortOrNull()
            ?: throw BadRequestException("unitId wajib diisi")

        val search = call.request.queryParameters["search"]

        val unit = unitService.getById(unitId) // validasi unit ada, throw NotFound kalau tidak

        call.respond(
            PebbleContent(
                TEMPLATE_FORM,
                mapOf(
                    "unit" to unit,
                    "users" to userUnitService.getAvailableUsersForUnit(unitId, search)
                )
            )
        )
    }

    // Submit form -> tambahkan user-user terpilih ke user_units
    suspend fun store(call: ApplicationCall) {

        val parameters = call.receiveParameters()

        val unitId = parameters["unitId"]?.toShortOrNull()
            ?: throw BadRequestException("unitId wajib diisi")

        val userIds = parameters.getAll("userIds")
            ?.mapNotNull { it.toShortOrNull() }
            ?.distinct()
            ?: emptyList()

        if (userIds.isEmpty()) {
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf("userIds" to "Pilih minimal satu user"),
                formData = mapOf("unitId" to unitId.toString()),
                formElement = formContext(unitId),
                mode = "add"
            )
        }

        try {
            val unit = unitService.getById(unitId)

            val added = userUnitService.addUsersToUnit(unitId, userIds)

            logger.info("Added {} user(s) to unit {} (id: {})", added, unit.nama, unitId)

            call.hxTriggerWithToast(
                "$added user BERHASIL ditambahkan ke unit ${unit.nama}.",
                ToastType.SUCCESS,
                "user-saved"
            )
            call.respond(HttpStatusCode.Created)

        } catch (ex: ApplicationException) {
            logger.error("Failed to add users to unit (unitId: {})", unitId, ex)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf("userIds" to (ex.message ?: "Ada kesalahan")),
                formData = mapOf("unitId" to unitId.toString()),
                formElement = formContext(unitId),
                mode = "add"
            )
        }
    }

    suspend fun delete(call: ApplicationCall, userId: Short, unitId: Short) {
        try {
            userUnitService.deleteUserUnit(userId, unitId)
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

    // HELPER: reload data untuk re-render form saat validasi gagal
    private suspend fun formContext(unitId: Short): Map<String, Any> = mapOf(
        "unit" to unitService.getById(unitId),
        "users" to userUnitService.getAvailableUsersForUnit(unitId)
    )
}