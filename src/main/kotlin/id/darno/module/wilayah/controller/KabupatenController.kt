package id.darno.module.wilayah.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.http.mapper.toFormData
import id.darno.core.pageddata.helper.pagedQueryParameters
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.session.model.UserSession
import id.darno.core.validation.valiktor.helper.errors
import id.darno.module.wilayah.helper.WilayahFormBuilder
import id.darno.module.wilayah.mapper.toCreateKabupatenParams
import id.darno.module.wilayah.mapper.toUpdateKabupatenParams
import id.darno.module.wilayah.service.KabupatenService
import id.darno.module.wilayah.service.ProvinsiService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.slf4j.LoggerFactory
import org.valiktor.ConstraintViolationException

class KabupatenController(
    private val kabupatenService: KabupatenService,
    private val provinsiService: ProvinsiService
) {

    private val logger =
        LoggerFactory.getLogger(KabupatenController::class.java)

    companion object {
        private const val TEMPLATE_PAGE =
            "pages/wilayah/kabupaten.html"

        private const val TEMPLATE_TABLE =
            "pages/wilayah/fragments/kabupaten-table.html"

        private const val TEMPLATE_FORM =
            "pages/wilayah/fragments/kabupaten-form.html"

        private const val PAGE_TITLE =
            "Daftar Kabupaten"

        // Default tampilan awal
        private const val DEFAULT_PROVINSI_ID: Short = 14

        // Untuk Kecamatan & Kelurahan
        private const val TEMPLATE_OPTIONS =
            "pages/wilayah/fragments/kabupaten-options.html"
    }

    suspend fun index(call: ApplicationCall) {

        val query = call.pagedQueryParameters()

        val provinsiList = provinsiService.getAllActive()

        val requestedProvinsiId =
            call.request.queryParameters["provinsiId"]
                ?.toShortOrNull()

        val selectedProvinsiId: Short =
            requestedProvinsiId
                ?.takeIf { id -> provinsiList.any { it.id == id } }
                ?: DEFAULT_PROVINSI_ID

        val result =
            kabupatenService.getTable(query, selectedProvinsiId)

        call.respondPebblePage(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "result" to result,
                "params" to query,
                "provinsiList" to provinsiList,
                "selectedProvinsiId" to selectedProvinsiId
            )
        )
    }

    suspend fun table(call: ApplicationCall) {

        val query = call.pagedQueryParameters()

        val provinsiId =
            call.request.queryParameters["provinsiId"]
                ?.toShortOrNull()

        val result =
            kabupatenService.getTable(query, provinsiId)

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

    suspend fun form(call: ApplicationCall) {

        val parameters = call.request.queryParameters
        val id = parameters["id"]?.toShortOrNull()
        val mode = parameters["mode"] ?: "add"

        val filterProvinsiId =
            parameters["provinsiId"]?.toShortOrNull()
                ?: DEFAULT_PROVINSI_ID

        val formData =
            id?.let { loadFormData(it) }
                ?: mapOf("provinsiId" to filterProvinsiId)

        call.respond(
            PebbleContent(
                TEMPLATE_FORM,
                mapOf(
                    "mode" to mode,
                    "errors" to emptyMap<String, String>(),
                    "formData" to formData,
                    "formElement" to formContext()
                )
            )
        )
    }

    suspend fun create(call: ApplicationCall) {

        val parameters =
            call.receiveParameters()

        val session =
            call.sessions.get<UserSession>()
                ?: throw ApplicationException(
                    "Session tidak ditemukan"
                )

        try {

            val request =
                WilayahFormBuilder.createKabupaten(parameters)

            val kabupaten =
                kabupatenService.create(
                    request.toCreateKabupatenParams(
                        session.userId
                    )
                )

            call.hxTriggerWithToast(
                "Kabupaten ${kabupaten.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "kabupaten-saved"
            )

            call.respond(HttpStatusCode.Created)

        } catch (ex: ConstraintViolationException) {

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = ex.errors(),
                formData = parameters.toFormData(),
                formElement = formContext(),
                mode = "add"
            )

        } catch (ex: ApplicationException) {

            logger.error("Failed to create kabupaten", ex)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    mapErrorKey(ex) to (
                            ex.message ?: "Ada kesalahan"
                    )
                ),
                formData = parameters.toFormData(),
                formElement = formContext(),
                mode = "add"
            )
        }
    }

    suspend fun update(call: ApplicationCall, id: Short) {

        val session =
            call.sessions.get<UserSession>()
                ?: throw ApplicationException(
                    "Session tidak ditemukan"
                )

        val parameters =
            call.receiveParameters()

        try {

            val request =
                WilayahFormBuilder.updateKabupaten(parameters)

            val kabupaten =
                kabupatenService.update(
                    id,
                    request.toUpdateKabupatenParams(
                        session.userId
                    )
                )

            logger.info(
                "Kabupaten updated successfully: {} (id: {})",
                kabupaten.nama,
                id
            )

            call.hxTriggerWithToast(
                "Kabupaten ${kabupaten.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "kabupaten-saved"
            )

            call.respond(HttpStatusCode.OK)

        } catch (ex: ConstraintViolationException) {

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = ex.errors(),
                formData = parameters.toFormData() + ("id" to id.toString()),
                formElement = formContext(),
                mode = "edit"
            )

        } catch (ex: ApplicationException) {

            logger.error("Failed to update kabupaten (id: $id)", ex)

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    mapErrorKey(ex) to (
                            ex.message ?: "Ada kesalahan"
                            )
                ),
                formData = parameters.toFormData() + ("id" to id.toString()),
                formElement = formContext(),
                mode = "edit"
            )
        }
    }

    suspend fun delete(call: ApplicationCall, id: Short) {
        try {
            kabupatenService.delete(id)

            call.hxTriggerWithToast(
                "Kabupaten BERHASIL dihapus.",
                ToastType.SUCCESS,
                "kabupaten-deleted"
            )
            call.respond(HttpStatusCode.NoContent)

        } catch (ex: ApplicationException) {
            logger.error("Failed to delete kabupaten (id: {})", id, ex)

            call.hxTriggerWithToast(
                ex.message ?: "Kabupaten GAGAL dihapus.",
                ToastType.ERROR
            )
            call.respond(HttpStatusCode.NoContent)
        }
    }

    /**
     * untuk mengisi ulang <option> dropdown Kabupaten saat Provinsi di form Kecamatan diganti
     */
    suspend fun options(call: ApplicationCall) {

        val provinsiId =
            call.request.queryParameters["provinsiId"]
                ?.toShortOrNull()

        val selectedId: Short =
            call.request.queryParameters["selected"]
                ?.toShortOrNull()
                ?: -1

        val kabupatenList =
            provinsiId
                ?.let { kabupatenService.getAllActiveByProvinsi(it) }
                ?: emptyList()

        call.respond(
            PebbleContent(
                TEMPLATE_OPTIONS,
                mapOf(
                    "kabupatenList" to kabupatenList,
                    "selectedId" to selectedId
                )
            )
        )
    }

    // HELPER

    private fun mapErrorKey(ex: ApplicationException): String {
        val msg = ex.message?.lowercase().orEmpty()
        return when {
            "provinsi" in msg -> "provinsiId"
            "kode" in msg -> "kode"
            else -> "nama"
        }
    }

    private suspend fun formContext(): Map<String, Any> = mapOf(
        "provinsiList" to provinsiService.getAllActive()
    )

    private suspend fun loadFormData(id: Short): Map<String, Any> {
        val kabupaten = kabupatenService.getById(id)

        return mapOf(
            "id" to kabupaten.id,
            "provinsiId" to kabupaten.provinsiId,
            "kode" to kabupaten.kode,
            "nama" to kabupaten.nama,
            "isActive" to kabupaten.isActive
        )
    }
}