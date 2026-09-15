package id.darno.module.wilayah.controller

import id.darno.core.exceptions.ApplicationException
import id.darno.core.htmx.exception.HtmxFormException
import id.darno.core.htmx.model.ToastType
import id.darno.core.htmx.utility.hxTriggerWithToast
import id.darno.core.http.mapper.toFormData
import id.darno.core.pageddata.helper.pagedQueryParameters
import id.darno.core.pebble.helper.respondPebblePage
import id.darno.core.session.model.UserSession
import id.darno.core.validation.toErrorMap
import id.darno.module.wilayah.helper.WilayahFormBuilder
import id.darno.module.wilayah.mapper.toCreateProvinsiParams
import id.darno.module.wilayah.mapper.toUpdateProvinsiParams
import id.darno.module.wilayah.service.ProvinsiService
import id.darno.module.wilayah.validator.CreateProvinsiValidator
import id.darno.module.wilayah.validator.UpdateProvinsiValidator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.pebble.PebbleContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import org.slf4j.LoggerFactory

class ProvinsiController(
    private val provinsiService: ProvinsiService
) {

    private val logger =
        LoggerFactory.getLogger(ProvinsiController::class.java)

    companion object {
        private const val TEMPLATE_PAGE =
            "pages/wilayah/provinsi.html"

        private const val TEMPLATE_TABLE =
            "pages/wilayah/fragments/provinsi-table.html"

        private const val TEMPLATE_FORM =
            "pages/wilayah/fragments/provinsi-form.html"

        private const val PAGE_TITLE =
            "Daftar Provinsi"
    }

    suspend fun index(call: ApplicationCall) {

        val query =
            call.pagedQueryParameters()

        val result =
            provinsiService.getTable(query)

        call.respondPebblePage(
            TEMPLATE_PAGE,
            mapOf(
                "title" to PAGE_TITLE,
                "result" to result,
                "params" to query
            )
        )
    }

    suspend fun table(call: ApplicationCall) {

        val query =
            call.pagedQueryParameters()

        val result =
            provinsiService.getTable(query)

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

        val parameters =
            call.request.queryParameters

        val id =
            parameters["id"]?.toShortOrNull()

        val mode =
            parameters["mode"] ?: "add"

        val formData =
            id?.let { loadFormData(it) }
                ?: emptyMap()

        call.respond(
            PebbleContent(
                TEMPLATE_FORM,
                mapOf(
                    "mode" to mode,
                    "errors" to emptyMap<String, String>(),
                    "formData" to formData
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

        val request =
            WilayahFormBuilder.createProvinsi(parameters)

        val validationErrors =
            CreateProvinsiValidator.validate(request)

        if (validationErrors.isNotEmpty()) {
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData = parameters.toFormData(),
                mode = "add"
            )
        }

        try {

            val provinsi =
                provinsiService.create(
                    request.toCreateProvinsiParams(
                        session.userId
                    )
                )

            call.hxTriggerWithToast(
                "Provinsi ${provinsi.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "provinsi-saved"
            )

            call.respond(HttpStatusCode.Created)

        } catch (ex: ApplicationException) {

            logger.error(
                "Failed to create provinsi",
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    mapErrorKey(ex) to (
                            ex.message ?: "Ada kesalahan"
                            )
                ),
                formData = parameters.toFormData(),
                mode = "add"
            )
        }
    }

    suspend fun update(
        call: ApplicationCall,
        id: Short
    ) {

        val parameters =
            call.receiveParameters()

        val session =
            call.sessions.get<UserSession>()
                ?: throw ApplicationException(
                    "Session tidak ditemukan"
                )

        val request =
            WilayahFormBuilder.updateProvinsi(parameters)

        val validationErrors =
            UpdateProvinsiValidator.validate(request)

        if (validationErrors.isNotEmpty()) {
            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = validationErrors.toErrorMap(),
                formData =
                    parameters.toFormData() +
                            ("id" to id.toString()),
                mode = "edit"
            )
        }

        try {

            val provinsi =
                provinsiService.update(
                    id,
                    request.toUpdateProvinsiParams(
                        session.userId
                    )
                )

            logger.info(
                "Provinsi updated successfully: {} (id: {})",
                provinsi.nama,
                id
            )

            call.hxTriggerWithToast(
                "Provinsi ${provinsi.nama} BERHASIL disimpan.",
                ToastType.SUCCESS,
                "provinsi-saved"
            )

            call.respond(HttpStatusCode.OK)

        } catch (ex: ApplicationException) {

            logger.error(
                "Failed to update provinsi (id: $id)",
                ex
            )

            throw HtmxFormException(
                templatePath = TEMPLATE_FORM,
                errors = mapOf(
                    mapErrorKey(ex) to (
                            ex.message ?: "Ada kesalahan"
                            )
                ),
                formData =
                    parameters.toFormData() +
                            ("id" to id.toString()),
                mode = "edit"
            )
        }
    }

    suspend fun delete(
        call: ApplicationCall,
        id: Short
    ) {

        try {

            provinsiService.delete(id)

            call.hxTriggerWithToast(
                "Provinsi BERHASIL dihapus.",
                ToastType.SUCCESS,
                "provinsi-deleted"
            )

            call.respond(HttpStatusCode.NoContent)

        } catch (ex: ApplicationException) {

            logger.error(
                "Failed to delete provinsi (id: {})",
                id,
                ex
            )

            call.hxTriggerWithToast(
                ex.message
                    ?: "Provinsi GAGAL dihapus.",
                ToastType.ERROR
            )

            call.respond(HttpStatusCode.NoContent)
        }
    }

    private fun mapErrorKey(
        ex: ApplicationException
    ): String {

        val msg =
            ex.message?.lowercase().orEmpty()

        return when {
            "kode" in msg -> "kode"
            else -> "nama"
        }
    }

    private suspend fun loadFormData(
        id: Short
    ): Map<String, Any> {

        val provinsi =
            provinsiService.getById(id)

        return mapOf(
            "id" to provinsi.id,
            "kode" to provinsi.kode,
            "nama" to provinsi.nama,
            "isActive" to provinsi.isActive
        )
    }
}